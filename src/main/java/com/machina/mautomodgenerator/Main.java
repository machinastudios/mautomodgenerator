package com.machina.mautomodgenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nonnull;

import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.machina.mautomodgenerator.commands.CommandHandler;
import com.machina.mautomodgenerator.model.ModFile;
import com.machina.shared.SuperPlugin;
import com.machina.shared.config.ModConfig;
import com.machina.shared.factory.ModLogger;
import com.machina.shared.util.ModJarUtils.ModManifest;

/**
 * Automatic mod generator for Hytale
 */
public class Main extends SuperPlugin {
    /**
     * The singleton instance of the Main class
     */
    public static Main INSTANCE;

    /**
     * The logger for the mod
     */
    private final ModLogger logger = ModLogger.forMod(this);

    /**
     * The config for the mod
     */
    private final ModConfig config = ModConfig.forMod(this);

    public Main(@Nonnull JavaPluginInit init) {
        super(init);
    }

    public void start() {
        // Save the instance
        INSTANCE = this;

        // Register the command handler
        getCommandRegistry().registerCommand(new CommandHandler());

        // Add default configs
        addDefaults();

        // Build the mod
        buildMod();
    }
    
    /**
     * Add default configs
     */
    private void addDefaults() {
        // Add default configs
        config.addDefault("manifest.name", "AutoModGenerator", "The name of the mod");
        config.addDefault("manifest.group", "com.machina", "The group of the mod");
        config.addDefault("manifest.name", "mautomodgenerator-pack", "The name of the mod");
        config.addDefault("manifest.version", "1.0.0", "The version of the mod");
        config.addDefault("manifest.author", "Machina", "The author of the mod");
        config.addDefault("manifest.description", "A mod that generates mods automatically", "The description of the mod");

        // Add src directories list
        config.addDefault("src.directories", new ArrayList<>(), "All directories to copy files from");

        // Load the configs
        config.load();
    }

    /**
     * Build the mod ZIP file
     */
    public void buildMod() {
        // Get the src directories
        List<String> srcDirectories = config.getStringList("src.directories", new ArrayList<>());

        // Get the files directory
        Path filesDirectory = config.getDataDirectory().resolve("files");

        // Get the mods directory
        Path modsDirectory = Path.of("mods");

        // Create the mods directory if it doesn't exist
        if (!Files.exists(modsDirectory)) {
            try {
                Files.createDirectories(modsDirectory);
            } catch (IOException e) {
                logger.error("Failed to create the mods directory: %t", e);
            }
        }

        // Create the files directory if it doesn't exist
        if (!Files.exists(filesDirectory)) {
            try {
                Files.createDirectories(filesDirectory);
            } catch (IOException e) {
                logger.error("Failed to create the files directory: %t", e);
            }
        }

        // Supports `configDir/files` by default
        srcDirectories.add(filesDirectory.toString());

        // All files to be added to the mod
        List<ModFile> files = new ArrayList<>();

        // Iterate over the src directories
        for (String srcDirectory : srcDirectories) {
            // Iterate over the directory
            List<File> srcFiles = iterateDirectory(new File(srcDirectory));

            // Add the src files to the files list
            files.addAll(
                srcFiles.stream()
                    .map(file ->
                        new ModFile(file, new File(srcDirectory))
                    )
                    .collect(Collectors.toList())
            );
        }

        // Build the mod manifest
        ModManifest manifest = buildModManifest();

        // Set the manifest main - use fixed package path
        String dummyMainPath = "com/machina/mautomodgenerator/DummyMain.class";

        // Create the .jar (zip) file name
        // "Why not using .zip?"
        // Because Hytale doesn't like reloading .zip files
        // cuz' they use `findResource` internally to load the resources
        // and .zip files are not supported by `findResource`
        // so we use .jar files instead
        String outputFileName = manifest.Group + "-" + manifest.Name + ".jar";

        FileOutputStream outputStream = null;
        ZipOutputStream zipOutputStream = null;

        try {
            // Create the mod ZIP
            outputStream = new FileOutputStream(modsDirectory.resolve(outputFileName).toFile());
            zipOutputStream = new ZipOutputStream(outputStream);

            // Add the manifest.json file to the ZIP
            ZipEntry manifestEntry = new ZipEntry("manifest.json");
            zipOutputStream.putNextEntry(manifestEntry);
            zipOutputStream.write(manifest.toJsonString().getBytes(StandardCharsets.UTF_8));
            zipOutputStream.closeEntry();

            // Add the DummyMain.class file to the ZIP
            // This is needed so the generated mod can load the class when it's loaded
            addDummyMainClass(zipOutputStream, dummyMainPath);

            // Add the files to the ZIP
            for (ModFile file : files) {
                // Get the file name related to the root
                logger.info("File %s -> %s", file.getRelativePath(), file.getFile().getPath());

                // Add the file to the ZIP
                ZipEntry zipEntry = file.toZipEntry();
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(file.readBytes());
                zipOutputStream.closeEntry();
            }
        } catch (IOException e) {
            logger.error("Failed to create the mod file: %t", e);
        } finally {
            // Close the output stream
            if (zipOutputStream != null) {
                try {
                    zipOutputStream.close();
                } catch (IOException e) {
                    logger.error("Failed to close the output stream: %t", e);
                }
            }

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    logger.error("Failed to close the output stream: %t", e);
                }
            }
        }

        logger.info("Mod file %s built successfully", outputFileName);
    }

    /**
     * Iterate over a directory
     * @param directory The directory to iterate over
     */
    private List<File> iterateDirectory(File directory) {
        // Get the files in the directory
        File[] files = directory.listFiles();

        // If the files is null, return an empty list
        if (files == null) {
            return new ArrayList<>();
        }

        // List of files to return
        List<File> filesList = new ArrayList<>();

        // Iterate over the files
        for (File file : files) {
            // If is a directory, iterate over the files in the directory
            if (file.isDirectory()) {
                filesList.addAll(iterateDirectory(file));
                continue;
            }

            // If is a file, copy the file to the output directory
            if (file.isFile()) {
                filesList.add(file);
            }
        }

        return filesList;
    }

    /**
     * Build the mod manifest
     */
    private ModManifest buildModManifest() {
        // Get the mod data
        String modGroup = config.getString("manifest.group");
        String modName = config.getString("manifest.name");
        String modVersion = config.getString("manifest.version");
        String modAuthor = config.getString("manifest.author");
        String modDescription = config.getString("manifest.description");

        // Validate the group and name
        // It should be file-compatible
        if (modGroup == null || !modGroup.matches("^[a-zA-Z0-9_.-]+$")) {
            throw new IllegalArgumentException("The group should be file-compatible");
        }

        if (modName == null || !modName.matches("^[a-zA-Z0-9_.-]+$")) {
            throw new IllegalArgumentException("The name should be file-compatible");
        }

        // Build the mod manifest
        ModManifest manifest = new ModManifest();
        manifest.Group = modGroup;
        manifest.Name = modName;
        manifest.Version = modVersion;
        manifest.Description = modDescription;
        manifest.Authors = new ArrayList<>();

        // Should include ourselves as a dependency
        manifest.Dependencies = new HashMap<>();
        manifest.Dependencies.put("com.machina:mautomodgenerator", "*");

        // Should always include the asset pack
        manifest.IncludesAssetPack = true;

        // Fake plugin main - keep it fixed to the original package
        manifest.Main = "com.machina.mautomodgenerator.DummyMain";

        // Create the author
        ModManifest.ModManifestAuthor author = new ModManifest.ModManifestAuthor();
        author.Name = modAuthor;
        manifest.Authors.add(author);

        // Return the manifest
        return manifest;
    }

    /**
     * Add the DummyMain.class file to the generated mod ZIP
     * This ensures the class is available when the mod is loaded
     * 
     * @param zipOutputStream The ZIP output stream to add the class to
     * @param classPath The target path for the class file in the ZIP (fixed to com/machina/mautomodgenerator/DummyMain.class)
     */
    private void addDummyMainClass(ZipOutputStream zipOutputStream, String classPath) {
        try {
            // Try multiple approaches to get the class file
            String existingClassPath = "com/machina/mautomodgenerator/DummyMain.class";
            InputStream classInputStream = null;

            // First, try to get it as a resource from the classloader
            classInputStream = getClass().getClassLoader().getResourceAsStream(existingClassPath);
            
            // If that fails, try using DummyMain.class directly
            if (classInputStream == null) {
                classInputStream = DummyMain.class.getResourceAsStream("/" + existingClassPath);
            }

            // If still null, try without the leading slash
            if (classInputStream == null) {
                classInputStream = DummyMain.class.getResourceAsStream("DummyMain.class");
            }

            // If still null, try from the current class's classloader
            if (classInputStream == null) {
                classInputStream = Main.class.getResourceAsStream("/" + existingClassPath);
            }

            if (classInputStream == null) {
                logger.warn("Could not find DummyMain.class resource. The generated mod may not be able to load the DummyMain class.");
                logger.warn("Tried paths: %s, /%s, DummyMain.class", existingClassPath, existingClassPath);
                return;
            }

            // Create the ZIP entry for the class file
            ZipEntry classEntry = new ZipEntry(classPath);
            zipOutputStream.putNextEntry(classEntry);

            // Copy the class file bytes to the ZIP (no modification needed - using original package)
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = classInputStream.read(buffer)) != -1) {
                zipOutputStream.write(buffer, 0, bytesRead);
            }

            zipOutputStream.closeEntry();
            classInputStream.close();

            logger.debug("Added DummyMain.class to the generated mod ZIP at %s", classPath);
        } catch (IOException e) {
            logger.error("Failed to add DummyMain.class to the mod ZIP: %t", e);
        }
    }
}