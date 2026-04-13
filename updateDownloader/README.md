# Update Downloader

This tool allows players to download and install updates for a modpack.
The updates should be created with the [Modpack Exporter](../modpackExporter/README.md)
and hosted on a webserver.

## Prerequisites

- Java 17

## Usage (Player)

1. Download the latest release from the [releases page](https://github.com/JavaRacing1/ModpackManagementTools/releases)
2. Place the downloaded jar file in the same directory as the modpack
3. Execute the jar file

## Usage (Modpack Creator)

1. Copy the [updateDownloader.toml](/src/main/resources/updateDownloader.toml) to the config directory of your modpack
   (and add it to your Git repository)
2. Change the configuration in the updateDownloader.toml. See [Configuration](#configuration)
3. Download the [versions.json](/src/main/resources/versions.json) and host it on a webserver.
   The URL of the file needs to be "https://{hostUrl}/{modpackName}/versions.json"
4. When releasing a new version of your modpack:
    1. Set the new version number in your [updateDownloader.toml](/src/main/resources/updateDownloader.toml)
       configuration
    2. Build the update archive with the [Modpack Exporter](../modpackExporter/README.md) and upload it to the webserver
    3. Add an entry to your [versions.json](/src/main/resources/versions.json) file with the new version number,
       download URL and release date

### SHA256 verification

SHA256 verification is an optional feature that ensures the integrity of the downloaded modpack update.
If enabled, the downloader will verify the SHA256 hash of the downloaded update against the hash provided by the
`versions.json` file. This helps prevent potential tampering or corruption of the update files during transmission.

If the version contains the `sha256` property, the downloader will use the value of this property to verify the SHA256
hash. If the property is not present and `sha256Url` is set, the downloader will attempt to fetch the hash
from the URL specified. If neither property is present, the SHA256 hash of the update will not be verified.

## Configuration

| Key                    | Type         | Default | Description                                            |
|------------------------|--------------|---------|--------------------------------------------------------|
| `version`              | String       | `-`     | The current version of the modpack installed. Required |
| `modpackName`          | String       | `-`     | The name of the modpack. Required                      |
| `hostUrl`              | String (URL) | `-`     | The URL of the update host server. Required            |
| `maxParallelDownloads` | Integer      | `5`     | The maximum number of concurrent downloads allowed.    |
