package de.javaracing.updateDownloader

import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Path

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
    val config = Config.load(configPath.toFile())
    try {
        config.validateConfig()
    } catch (e: IllegalArgumentException) {
        logger.error(e) { "Config validation failed: ${e.message}" }
        return
    }
}
