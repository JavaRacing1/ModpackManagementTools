package de.javaracing.modpack_exporter

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.addResourceSource
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Represents the configuration for the modpack exporter.
 *
 * @property repositoryPath The file system path to the Git repository being analyzed.
 * @property oldVersionTag The tag name representing the old version in the Git repository.
 * @property newVersionTag The tag name representing the new version in the Git repository.
 * @property outputDir The directory where the update files will be placed.
 * @property checkoutNewVersion Whether to check out the new version of the repository.
 */
data class Config(val repositoryPath: String, val oldVersionTag: String, val newVersionTag: String, val outputDir: String,
                  val checkoutNewVersion: Boolean) {
    val oldVersionTagRef: String
        get() = "refs/tags/$oldVersionTag"

    val newVersionTagRef: String
        get() = "refs/tags/$newVersionTag"

    companion object {
        /**
         * Loads a configuration from the specified resource.
         *
         * @param resource The path to the resource containing the configuration.
         * @return The loaded configuration as an instance of the Config class.
         * @throws Exception If the configuration cannot be loaded or validation fails.
         */
        @OptIn(ExperimentalHoplite::class)
        fun load(resource: String): Config = ConfigLoaderBuilder.default()
            .addResourceSource(resource)
            .addOnFailureCallback { logger.error { "Failed to load config from resource $resource: $it" } }
            .withExplicitSealedTypes()
            .build()
            .loadConfigOrThrow<Config>()
    }
}
