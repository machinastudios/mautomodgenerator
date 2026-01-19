# MAutoModGenerator - Automatic Mod Generator for Hytale

An automated mod generator plugin for Hytale servers that automatically builds mod ZIP files from configured source directories.

## Overview

MAutoModGenerator is a plugin that automatically generates Hytale mod ZIP files by collecting files from configured source directories and creating a proper mod package with `manifest.json`. This is useful for packaging custom content, assets, or configurations into distributable mod files.

## Features

- **Automatic Mod Building**: Automatically builds mod ZIP files on plugin initialization
- **Configurable Source Directories**: Supports multiple source directories for collecting files
- **Automatic Manifest Generation**: Generates `manifest.json` based on configuration
- **Command-Based Rebuild**: Rebuild mods on-demand using the `/mautomodgenerator rebuild` command
- **Default Source Directory**: Automatically includes files from `configDir/files` directory

## Installation

1. Place the MAutoModGenerator plugin JAR file in your Hytale server's `mods` or `builtin` directory
2. Ensure you have the `HytaleServer.jar` file in the `lib/` directory
3. Restart the server or reload plugins
4. The plugin will automatically build the mod on first load

## Configuration

All settings are managed through the `config.json5` file in the plugin's data directory.

### Manifest Configuration

```json5
{
    "manifest": {
        "group": "com.machina",
        "name": "mautomodgenerator-pack",
        "version": "1.0.0",
        "author": "Machina",
        "description": "A mod that generates mods automatically"
    }
}
```

**Configuration Options:**
- `manifest.group`: The mod group identifier (must be file-compatible: `^[a-zA-Z0-9_.-]+$`)
- `manifest.name`: The mod name (must be file-compatible: `^[a-zA-Z0-9_.-]+$`)
- `manifest.version`: The mod version (e.g., "1.0.0")
- `manifest.author`: The author name
- `manifest.description`: The mod description

### Source Directories Configuration

```json5
{
    "src": {
        "directories": [
            "/path/to/custom/files",
            "/another/path/to/files"
        ]
    }
}
```

**Configuration Options:**
- `src.directories`: List of directories to collect files from (optional)
  - The plugin automatically includes `configDir/files` by default
  - Additional directories can be specified in this list
  - All files from these directories will be included in the generated mod ZIP

## Usage

### Automatic Building

The mod is automatically built when:
- The plugin is first loaded
- The server starts with the plugin enabled

The generated mod ZIP file will be created in the server's root directory with the format: `{group}-{name}.zip`

Example: `com.machina-mautomodgenerator-pack.zip`

### Manual Rebuild

You can manually rebuild the mod at any time using the command:

```
/mautomodgenerator rebuild
```

Or using the alias:

```
/mamg rebuild
```

**Permission**: Requires `mautomodgenerator.command.rebuild.permission`

## How It Works

1. **File Collection**: The plugin scans all configured source directories (including the default `configDir/files`)
2. **Manifest Generation**: Creates a `manifest.json` file based on your configuration
3. **ZIP Creation**: Packages all collected files along with the manifest into a ZIP file
4. **Output**: The generated mod ZIP is saved in the server root directory

### File Structure

The generated mod ZIP will have the following structure:

```
mod.zip
├── manifest.json
├── (files from source directories)
└── ...
```

## Example Use Cases

- **Asset Packs**: Package custom textures, models, or sounds into a distributable mod
- **Configuration Mods**: Bundle server configurations or custom game rules
- **Content Packs**: Create mods with custom content that can be easily distributed
- **Development Workflow**: Automatically package mods during development

## Notes

- The generated mod ZIP file is created in the server's root directory
- File paths in the ZIP are relative to the source directories
- The plugin validates that group and name are file-compatible (alphanumeric, dots, underscores, hyphens only)
- All files from source directories are included recursively (subdirectories are processed)

---

**Developed by Machina Studios** - Professional Hytale Server Solutions
