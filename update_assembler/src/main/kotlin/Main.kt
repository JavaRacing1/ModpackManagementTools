package de.javaracing.update_assembler

import io.github.oshai.kotlinlogging.KotlinLogging
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.lib.Repository
import java.io.File
import java.io.IOException

private const val repositoryPath = ""
private const val oldVersionTag = ""
private const val newVersionTag = ""

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "Loading Git repository at $repositoryPath" }
    val repositoryFile = File(repositoryPath)
    var repository: Repository?
    try {
        repository = loadRepository(repositoryFile)
    } catch (e: IOException) {
        logger.error(e) { "Could not load Git repository at $repositoryPath" }
        return
    }

    logger.info { "Calculating diff between old version ($oldVersionTag) and new version ($newVersionTag)" }
    val diffEntries: List<DiffEntry> = determineDiff(repository, oldVersionTag, newVersionTag)
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
}