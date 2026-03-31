package de.javaracing.modpackExporter.exporter

import de.javaracing.modpackExporter.Config
import de.javaracing.modpackExporter.exception.TagNotFoundException
import de.javaracing.modpackExporter.util.calculateHashAndSave
import de.javaracing.modpackExporter.util.determineDiff
import de.javaracing.modpackExporter.util.getChangedFilePaths
import de.javaracing.modpackExporter.util.getOutdatedFilePaths
import io.github.oshai.kotlinlogging.KotlinLogging
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.zeroturnaround.zip.ZipUtil
import java.io.File
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.writeLines

/**
 * Implementation of the ModpackExporter interface for generating updates by calculating
 * and packaging the changes between two versions of a Git repository.
 */
class UpdateExporter : ModpackExporter {
    private val logger = KotlinLogging.logger {}
    private var tempDirPath: Path? = null

    override fun getName(): String = "update_exporter"

    override fun setTempDirectory(tempDirectory: Path) {
        tempDirPath = tempDirectory
    }

    override fun getSubTempDirectory(): Path =
        tempDirPath?.resolve(getName()) ?: throw IllegalStateException("Temp directory not set")

    override fun prepareFiles(git: Git, config: Config) {
        val versionTag = config.versionTag
        val previousVersionTag = config.previousVersionTag
        logger.info { "Calculating diff between updated version (${versionTag}) and previous version (${previousVersionTag})" }
        var diffEntries: List<DiffEntry>
        try {
            diffEntries = determineDiff(git.repository, config.versionTagRef, config.previousVersionTagRef)
        } catch (e: IOException) {
            logger.error(e) {
                "Could not calculate diff between updated version (${versionTag}) and previous version (${previousVersionTag})"
            }
            return
        } catch (e: TagNotFoundException) {
            logger.error(e) { "Could not find tag (${e.tagName})" }
            return
        }
        val changedFilePaths: Set<String> = getChangedFilePaths(diffEntries)
        val outdatedFilePaths: Set<String> = getOutdatedFilePaths(diffEntries)

        logger.info { "Changed files:" }
        changedFilePaths.forEach {
            logger.info { "+ $it" }
        }
        logger.info { "Outdated files:" }
        outdatedFilePaths.forEach {
            logger.info { "- $it" }
        }

        val tempDirPath = getSubTempDirectory()
        logger.info { "Copying changed files to temp directory $tempDirPath" }
        changedFilePaths.forEach { filePath ->
            val repositoryFile = File(config.repositoryPath, filePath)
            val newFile = File(tempDirPath.toFile(), filePath)
            repositoryFile.copyTo(newFile, overwrite = true)
        }

        writeDeleteOutdatedFilesBatchScript(outdatedFilePaths, tempDirPath)
        writeDeleteOutdatedFilesShellScript(outdatedFilePaths, tempDirPath)
        writeOutdatedFiles(outdatedFilePaths, tempDirPath)
    }

    private fun writeDeleteOutdatedFilesBatchScript(outdatedFilePaths: Set<String>, tempDirPath: Path) {
        logger.info { "Writing batch script for deleting outdated files" }
        val fileLines = mutableListOf<String>()
        fileLines.add("@echo off")
        fileLines.add("echo Deleting files...")
        outdatedFilePaths.forEach { filePath ->
            fileLines.add("del /f /q \".\\${filePath.replace('/', '\\')}\"")
        }
        fileLines.add("echo All outdated files deleted.")
        fileLines.add("pause")

        val batchFilePath = tempDirPath.resolve("delete_outdated_files.bat")
        batchFilePath.writeLines(fileLines)
    }

    private fun writeDeleteOutdatedFilesShellScript(outdatedFilePaths: Set<String>, tempDirPath: Path) {
        logger.info { "Writing shell script for deleting outdated files" }
        val fileLines = mutableListOf<String>()
        fileLines.add("#!/bin/sh")
        fileLines.add("echo Deleting files...")
        outdatedFilePaths.forEach { filePath ->
            fileLines.add("rm -f \"$filePath\"")
        }
        fileLines.add("echo All outdated files deleted.")

        val shellFilePath = tempDirPath.resolve("delete_outdated_files.sh")
        shellFilePath.writeLines(fileLines)
    }

    private fun writeOutdatedFiles(outdatedFilePaths: Set<String>, tempDirPath: Path) {
        logger.info { "Writing file with outdated files" }
        val fileLines = mutableListOf<String>()
        outdatedFilePaths.forEach { filePath ->
            fileLines.add(filePath)
        }
        val outdatedFilesFilePath = tempDirPath.resolve("outdated_files.txt")
        outdatedFilesFilePath.writeLines(fileLines)
    }

    override fun export(modpackName: String, config: Config) {
        val updateZipPath = Path(config.outputDir).resolve("${modpackName}_${config.versionTag}_Update.zip")
        if (updateZipPath.exists()) {
            logger.info { "Deleting existing update file $updateZipPath" }
            updateZipPath.toFile().delete()
        }
        logger.info { "Packing update to $updateZipPath" }
        ZipUtil.pack(getSubTempDirectory().toFile(), updateZipPath.toFile())
        val hash = calculateHashAndSave(updateZipPath)
        logger.info { "SHA-256 Hash of zip: $hash" }
    }
}
