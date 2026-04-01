package de.javaracing.modpackExporter

import de.javaracing.modpackExporter.exporter.ModpackExporter
import de.javaracing.modpackExporter.exporter.UpdateExporter
import de.javaracing.modpackExporter.util.checkout
import de.javaracing.modpackExporter.util.loadRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import java.io.IOException
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.name

private const val CONFIG_RESOURCE = "/modpackExporter.toml"
private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "Loading config at resource $CONFIG_RESOURCE" }
    val config = Config.load(CONFIG_RESOURCE)
    try {
        config.validateConfig()
    } catch (e: IllegalArgumentException) {
        logger.error(e) { "Config validation failed: ${e.message}" }
        return
    }

    //TODO: Load exporters from config
    val exporters: List<ModpackExporter> = listOf(UpdateExporter())

    //TODO: Support remote repositories
    val repositoryPath = Path(config.repositoryPath)
    logger.info { "Loading Git repository at $repositoryPath" }
    var repository: Repository?
    try {
        repository = loadRepository(repositoryPath.toFile())
    } catch (e: IOException) {
        logger.error(e) { "Could not load Git repository at $repositoryPath" }
        return
    }
    val git = Git(repository)

    //TODO: Provide more settings for checkout
    if (config.checkoutVersion) {
        logger.info { "Checking out updated version (${config.versionTag})" }
        checkout(git, config.versionTagRef)
    }

    logger.info { "Preparing files for export" }
    val tempDirPath = createTempDirectory("de/javaracing/modpackExporter")
    for (exporter in exporters) {
        exporter.setTempDirectory(tempDirPath)
        exporter.getSubTempDirectory().createDirectories()
        exporter.prepareFiles(git, config)
    }
    logger.info { "Files prepared for export" }

    val outputDir = config.outputDir
    val outputDirPath = Path(outputDir)
    if (!outputDirPath.exists()) {
        logger.info { "Creating output directory $outputDir" }
        outputDirPath.createDirectories()
    } else if (!outputDirPath.isDirectory()) {
        logger.error { "Output directory $outputDir is not a directory" }
        return
    }

    logger.info { "Exporting modpack to $outputDir" }
    for (exporter in exporters) {
        exporter.export(repositoryPath.name, config)
    }
    logger.info { "Modpack export complete" }

    repository.close()
}
