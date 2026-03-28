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
 * @property versionTag The tag name representing the updated version in the Git repository.
 * @property previousVersionTag The tag name representing the previous version in the Git repository.
 * @property outputDir The directory where the update files will be placed.
 * @property checkoutVersion Whether to check out the updated version of the repository.
 */
data class Config(
    val repositoryPath: String, val versionTag: String, val previousVersionTag: String, val outputDir: String,
    val checkoutVersion: Boolean
) {
    val versionTagRef: String
        get() = "refs/tags/$versionTag"

    val previousVersionTagRef: String
        get() = "refs/tags/$previousVersionTag"

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
