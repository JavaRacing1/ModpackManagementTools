package de.javaracing.modpack_exporter.exporter

import de.javaracing.modpack_exporter.Config
import de.javaracing.modpack_exporter.util.calculateHashAndSave
import de.javaracing.modpack_exporter.util.determineDiff
import de.javaracing.modpack_exporter.util.getChangedFilePaths
import de.javaracing.modpack_exporter.util.getDeletedFilePaths
import io.github.oshai.kotlinlogging.KotlinLogging
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.zeroturnaround.zip.ZipUtil
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists

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
        logger.info { "Calculating diff between updated version (${config.versionTag}) and previous version (${config.previousVersionTag})" }
        var diffEntries: List<DiffEntry>
        try {
            diffEntries = determineDiff(git.repository, config.versionTagRef, config.previousVersionTagRef)
        } catch (e: Exception) {
            logger.error(e) { "Could not calculate diff between updated version (${config.versionTag}) and previous version (${config.previousVersionTag})" }
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

        val tempDirPath = getSubTempDirectory()
        logger.info { "Copying changed files to temp directory $tempDirPath" }
        changedFilePaths.forEach { filePath ->
            val repositoryFile = File(config.repositoryPath, filePath)
            val newFile = File(tempDirPath.toFile(), filePath)
            repositoryFile.copyTo(newFile, overwrite = true)
        }
        //TODO: Create delete files scripts
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