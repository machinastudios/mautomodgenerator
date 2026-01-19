package com.machina.mautomodgenerator.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;

public class ModFile {
    /**
     * The file
     */
    private final File file;

    /**
     * The relative path
     */
    private final File root;

    /**
     * Constructor
     * @param file The file
     * @param relativePath The relative path
     */
    public ModFile(File file, File root) {
        this.file = file;
        this.root = root;
    }

    /**
     * Get the relative path
     * @return The relative path
     */
    public String getRelativePath() {
        return file.getPath().replace(root.getPath() + File.separator, "");
    }

    /**
     * Get the file
     * @return The file
     */
    public File getFile() {
        return file;
    }

    /**
     * Get the root
     * @return The root
     */
    public File getRoot() {
        return root;
    }

    /**
     * Convert the file to a byte array
     * @return The byte array
     * @throws IOException If the file cannot be read
     */
    public byte[] readBytes() throws IOException {
        return Files.readAllBytes(file.toPath());
    }

    /**
     * Convert the file to a ZIP entry
     * @return The ZIP entry
     */
    public ZipEntry toZipEntry() {
        // Add the file to the ZIP
        ZipEntry zipEntry = new ZipEntry(getRelativePath());
        return zipEntry;
    }
}