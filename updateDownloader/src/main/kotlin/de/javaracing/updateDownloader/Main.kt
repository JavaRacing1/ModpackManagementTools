package de.javaracing.updateDownloader

import de.javaracing.updateDownloader.data.AvailableVersions
import de.javaracing.updateDownloader.data.VersionInfo
import de.javaracing.updateDownloader.util.downloadVersionData
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import java.nio.file.Path
import kotlin.io.path.createTempDirectory

private val logger = KotlinLogging.logger {}

fun main() {
    val currentPath = Path.of("").toAbsolutePath().normalize()
    val configPath = currentPath.resolve("config/config.toml")
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

    var availableVersions: AvailableVersions? = null
    runBlocking {
        try {
            availableVersions = downloadVersionData(client, modpackHostUrl)
        } catch (e: Exception) {
            logger.error(e) { "Failed to fetch available versions: ${e.message}" }
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

    logger.info { "${newerVersions.size} updates found: ${newerVersions.joinToString { it.version }}" }

    val tempDirPath = createTempDirectory("updateDownloader")
}
