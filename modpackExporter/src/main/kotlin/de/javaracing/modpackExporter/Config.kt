package de.javaracing.modpackExporter

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.addResourceSource
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Represents the configuration for the modpack exporter.
 *
 * @property repository The configuration for the Git repository being analyzed.
 * @property outputDir The directory where the update files will be placed.
 */
data class Config(
    val repository: RepositoryConfig,
    val outputDir: String
) {
    val versionTagRef: String get() = "refs/tags/${repository.versionTag}"

    val previousVersionTagRef: String get() = "refs/tags/${repository.previousVersionTag}"

    /**
     * Validates the configuration by checking if all required values are present and not blank.
     *
     * @throws IllegalArgumentException If any required value is missing or blank.
     */
    fun validateConfig() {
        for ((key, value) in getRequiredConfigValues()) {
            if (value.isBlank()) {
                throw IllegalArgumentException("Required config value '$key' is blank")
            }
        }
    }

    private fun getRequiredConfigValues(): Map<String, String> = mapOf(
        "[repository]repositoryPath" to repository.repositoryPath,
        "[repository]versionTag" to repository.versionTag,
        "outputDir" to outputDir
    )

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

/**
 * Represents the configuration for the Git repository.
 *
 * @property repositoryPath The file system path to the Git repository being analyzed.
 * @property versionTag The tag name representing the updated version in the Git repository.
 * @property previousVersionTag The tag name representing the previous version in the Git repository.
 * @property checkoutVersion Whether to check out the updated version of the repository.
 * @property forceCheckout Whether to force the checkout.
 * @property stashChanges Whether to stash changes before checkout.
 */
data class RepositoryConfig(
    val repositoryPath: String,
    val versionTag: String,
    val previousVersionTag: String,
    val checkoutVersion: Boolean = true,
    val forceCheckout: Boolean = false,
    val stashChanges: Boolean = true
)
