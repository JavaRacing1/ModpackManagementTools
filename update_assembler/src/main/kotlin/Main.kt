package de.javaracing.update_assembler

import io.github.oshai.kotlinlogging.KotlinLogging
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.lib.Repository
import org.zeroturnaround.zip.ZipUtil
import java.io.File
import java.io.IOException
import kotlin.io.path.*

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    val configRessource: String = if (args.isNotEmpty()) args[0] else "/update_assembler.toml"
    logger.info { "Loading config at resource $configRessource" }
    val config = Config.load(configRessource)

    val repositoryPath = config.repositoryPath
    logger.info { "Loading Git repository at $repositoryPath" }
    val repositoryFile = File(repositoryPath)
    var repository: Repository?
    try {
        repository = loadRepository(repositoryFile)
    } catch (e: IOException) {
        logger.error(e) { "Could not load Git repository at $repositoryPath" }
        return
    }

    logger.info { "Calculating diff between old version (${config.oldVersionTag}) and new version (${config.newVersionTag})" }
    var diffEntries: List<DiffEntry>
    try {
        diffEntries = determineDiff(repository, config.oldVersionTagRef, config.newVersionTagRef)
    } catch (e: Exception) {
        logger.error(e) { "Could not calculate diff between old version (${config.oldVersionTag}) and new version (${config.newVersionTag})" }
        return
    }
    val changedFilePaths: Set<String> = getChangedFilePaths(diffEntries)
    val deletedFilePaths: Set<String> = getDeletedFilePaths(diffEntries)

    logger.info { "Changed files:" }
    changedFilePaths.forEach {
        logger.info { "+ $it" }
    }
    logger.info { "Deleted files:" }
    deletedFilePaths.forEach {
        logger.info { "- $it" }
    }

    val git = Git(repository)
    if (config.checkoutNewVersion) {
        logger.info { "Checking out new version (${config.newVersionTag})" }
        git.checkout()
            .setName(config.newVersionTagRef)
            .setForced(true)
            .call()
    }

    val tempDirPath = createTempDirectory("modpack_update_assembler")
    logger.info { "Copying changed files to temp directory $tempDirPath" }
    changedFilePaths.forEach { filePath ->
        val repositoryFile = File(repositoryPath, filePath)
        val newFile = File(tempDirPath.toFile(), "update/$filePath")
        repositoryFile.copyTo(newFile, overwrite = true)
    }

    val outputDir = config.outputDir
    val outputDirPath = Path(outputDir)
    if (!outputDirPath.exists()) {
        logger.info { "Creating output directory $outputDir" }
        outputDirPath.createDirectories()
    } else if (!outputDirPath.isDirectory()) {
        logger.error { "Output directory $outputDir is not a directory" }
        return
    }

    val updateZipPath = outputDirPath.resolve("update_${config.newVersionTag}.zip")
    if (updateZipPath.exists()) {
        logger.info { "Deleting existing update file $updateZipPath" }
        updateZipPath.toFile().delete()
    }
    logger.info { "Packing update to $updateZipPath" }
    ZipUtil.pack(tempDirPath.resolve("update").toFile(), updateZipPath.toFile())

    repository.close()
}