package de.javaracing.modpack_exporter.exporter

import de.javaracing.modpack_exporter.Config
import org.eclipse.jgit.api.Git
import java.nio.file.Path

/**
 * Interface for exporting modpacks using a specified set of configurations and a Git repository.
 *
 * Implementations of this interface define the behavior for preparing and exporting modpacks by
 * organizing the necessary files into the desired output format.
 */
interface ModpackExporter {
    /**
     * Retrieves the name of the exporter implementation.
     *
     * @return The name of the exporter as a string.
     */
    fun getName(): String

    /**
     * Sets the temporary directory to be used by the exporter.
     *
     * @param tempDirectory The path to the directory that will serve as a temporary location.
     */
    fun setTempDirectory(tempDirectory: Path)

    /**
     * Retrieves a subdirectory within the temporary directory for use by the exporter.
     *
     * This method constructs a path based on the temporary directory that has been set
     * and appends the name of the exporter to it. If no temporary directory is set,
     * an exception is thrown.
     *
     * @return The path to the subdirectory within the temporary directory.
     * @throws IllegalStateException If the temporary directory has not been set.
     */
    fun getSubTempDirectory(): Path

    /**
     * Prepares the necessary files for the modpack export process.
     *
     * This method is responsible for setting up the required files and directories
     * needed for the modpack export. It uses the provided Git repository and configuration
     * to perform the necessary operations.
     *
     * @param git The Git repository instance to be used for operations.
     * @param config The configuration settings for the modpack export.
     */
    fun prepareFiles(git: Git, config: Config)

    /**
     * Exports a modpack using the specified configuration.
     *
     * This method finalizes the modpack export process by packing the required files
     * into a designated output format. It ensures any existing export file in the output
     * directory is removed and creates a new one using the provided configuration parameters.
     *
     * @param modpackName The name of the modpack being exported.
     * @param config The configuration settings for the modpack export.
     */
    fun export(modpackName: String, config: Config)
}
