package de.javaracing.update_assembler

import de.javaracing.update_assembler.exporter.ModpackExporter
import de.javaracing.update_assembler.exporter.UpdateExporter
import de.javaracing.update_assembler.util.checkout
import de.javaracing.update_assembler.util.loadRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import java.io.IOException
import kotlin.io.path.*

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    val configRessource: String = if (args.isNotEmpty()) args[0] else "/update_assembler.toml"
    logger.info { "Loading config at resource $configRessource" }
    val config = Config.load(configRessource)

    val exporters: List<ModpackExporter> = listOf(UpdateExporter())

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

    if (config.checkoutNewVersion) {
        logger.info { "Checking out new version (${config.newVersionTag})" }
        checkout(git, config.newVersionTagRef)
    }

    logger.info { "Preparing files for export" }
    val tempDirPath = createTempDirectory("modpack_update_assembler")
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