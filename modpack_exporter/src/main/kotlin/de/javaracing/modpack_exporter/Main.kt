package de.javaracing.modpack_exporter

import de.javaracing.modpack_exporter.exporter.ModpackExporter
import de.javaracing.modpack_exporter.exporter.UpdateExporter
import de.javaracing.modpack_exporter.util.checkout
import de.javaracing.modpack_exporter.util.loadRepository
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

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    //TODO: Allow config with named args
    val configRessource: String = if (args.isNotEmpty()) args[0] else "/modpack_exporter.toml"
    logger.info { "Loading config at resource $configRessource" }
    val config = Config.load(configRessource)

    //TODO: Load exporters from config
    //TODO: Add curseforge exporter
    //TODO: Add technic launcher exporter
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
    val tempDirPath = createTempDirectory("de/javaracing/modpack_exporter")
    exporters.forEach { exporter ->
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
    exporters.forEach { exporter ->
        exporter.export(repositoryPath.name, config)
    }
    logger.info { "Modpack export complete" }

    repository.close()
}
