package de.javaracing.updateDownloader

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.addFileSource
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.net.URI

private val logger = KotlinLogging.logger {}

/**
 * Represents the configuration for the update downloader.
 *
 * @property version The current version of the modpack installed.
 * @property modpackName The name of the modpack.
 * @property hostUri The URI of the update host server.
 */
data class Config(val version: String, val modpackName: String, val hostUri: URI) {
    /**
     * Validates the configuration by checking if all required values are present and not blank.
     *
     * @throws IllegalArgumentException If any required value is missing or blank.
     */
    fun validateConfig() {
        for ((key, value) in getRequiredConfigValues()) {
            if (value.toString().isBlank()) {
                throw IllegalArgumentException("Required config value '$key' is blank")
            }
        }
    }

    private fun getRequiredConfigValues(): Map<String, Any> = mapOf(
        "version" to version,
        "modpackName" to modpackName,
        "hostUri" to hostUri
    )

    companion object {
        /**
         * Loads a configuration from the specified file.
         *
         * @param file The path to the file containing the configuration.
         * @return The loaded configuration as an instance of the Config class.
         * @throws Exception If the configuration cannot be loaded or validation fails.
         */
        @OptIn(ExperimentalHoplite::class)
        fun load(file: File): Config = ConfigLoaderBuilder.default()
            .addFileSource(file)
            .addOnFailureCallback { logger.error { "Failed to load config from file $file: $it" } }
            .withExplicitSealedTypes()
            .build()
            .loadConfigOrThrow<Config>()

        /**
         * Copies the default configuration file to the specified target location.
         *
         * @param targetFile The file where the default configuration should be copied to.
         * @throws IllegalStateException If the default configuration resource cannot be found.
         */
        fun copyDefaultConfig(targetFile: File) {
            Config::class.java.getResourceAsStream("/config.toml").use { input ->
                {
                    if (input == null) {
                        logger.error { "Failed to load default config: resource not found" }
                        throw IllegalStateException("Failed to load default config: resource not found")
                    }
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
            logger.info { "Copied default config to $targetFile" }
        }
    }
}
