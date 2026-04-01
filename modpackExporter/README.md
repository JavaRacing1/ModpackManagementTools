# Modpack Exporter

This tool allows you to export your Minecraft modpack from a Git repository to various formats.
It simplifies the process of distributing modpacks and modpack updates, making it easier for others to access, install
and update your modpack.

### Currently supported exports

- **Update only** – Calculates the difference between two versions of the modpack and creates a zip file containing only
  the files that have been modified or added since the older version.
  The generated zip file contains batch and shell scripts that can be used to delete outdated files from the target
  installation. The Git tags reference the modpack versions compared.

### Planned exports

- **Curseforge** (.zip Import)
- **Modrinth** (.mrpack Import)
- **Technic Launcher**

## Prerequisites

- Java 25
- Git

## Usage

1. Download the latest release from the releases page
2. Extract the archive
3. Change the configuration at `/config/modpackExporter.toml`
4. Start the application by running the script in the `/bin` directory

## Configuration

| Key                               | Type             | Default | Description                                                              |
|-----------------------------------|------------------|---------|--------------------------------------------------------------------------|
| `outputDir`                       | String (Path)    | `-`     | Directory where the output will be placed. Required                      |
| `[repository] repositoryPath`     | String (Path)    | `-`     | Path to the Git repository. Required                                     |
| `[repository] versionTag`         | String (Git Tag) | `-`     | Tag of the new modpack version. Required                                 |
| `[repository] previousVersionTag` | String (Git Tag) | `-`     | Tag of the previous modpack version. Required for the update only export |
| `[repository] checkoutVersion`    | Boolean          | `true`  | When true, checkout the tag of the new version before the export         |
| `[repository] forceCheckout`      | Boolean          | `false` | When true, force the checkout of the tag                                 |
| `[repository] stashChanges`       | Boolean          | `true`  | When true, stash changes before the checkout                             |
