package de.javaracing.updateDownloader

import de.javaracing.updateDownloader.data.AvailableVersions
import de.javaracing.updateDownloader.data.VersionInfo
import de.javaracing.updateDownloader.util.downloadAllUpdates
import de.javaracing.updateDownloader.util.downloadVersionData
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createTempDirectory

private val logger = KotlinLogging.logger {}

fun main() {
    val currentPath = Path.of("").toAbsolutePath().normalize()
    val configPath = currentPath.resolve("config/updateDownloader.toml")
    if (!configPath.toFile().exists()) {
        logger.warn { "Config file not found at $configPath. Creating default config." }
        Config.copyDefaultConfig(configPath.toFile())
        logger.warn { "Configure the update downloader and restart the application." }
        return
    }
    logger.info { "Loading config from $configPath" }
    val config = Config.load(configPath.toFile())
    try {
        config.validateConfig()
    } catch (e: IllegalArgumentException) {
        logger.error(e) { "Config validation failed: ${e.message}" }
        return
    }

    logger.info { "Current version: ${config.version}" }

    val client = OkHttpClient()
    val modpackHostUrl = config.hostUrl.toURI().resolve(config.modpackName).toURL()

    val availableVersions: AvailableVersions? = runBlocking {
        try {
            downloadVersionData(client, modpackHostUrl)
        } catch (e: Exception) {
            logger.error(e) { "Failed to fetch available versions: ${e.message}" }
            null
        }
    }
    if (availableVersions == null) {
        return
    }

    var newerVersions: List<VersionInfo>
    try {
        newerVersions = availableVersions.getNewerVersions(config.version)
    } catch (e: IllegalArgumentException) {
        logger.error(e) { "Failed to determine newer versions: ${e.message}" }
        return
    }

    if (newerVersions.isEmpty()) {
        logger.info { "No newer versions found. Modpack is up to date." }
        return
    }

    logger.info { "Updates found: ${newerVersions.size} (${newerVersions.joinToString { it.version }})" }

    val tempDirPath = createTempDirectory("updateDownloader")
    val updateFileMap: Map<String, File> = runBlocking {
        try {
            downloadAllUpdates(client, newerVersions, tempDirPath, config.maxParallelDownloads)
        } catch (e: Exception) {
            logger.error(e) { "Failed to download updates: ${e.message}" }
            emptyMap()
        }
    }

    if (updateFileMap.isEmpty()) {
        logger.info { "No updates downloaded" }
        return
    }

    logger.info { "Updates downloaded: ${updateFileMap.size}" }
}
