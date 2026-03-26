package de.javaracing.update_assembler

import io.github.oshai.kotlinlogging.KotlinLogging
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

private val logger = KotlinLogging.logger {}

/**
 * Loads a Git repository from the specified directory.
 *
 * @param repositoryDir The directory containing the Git repository to load.
 * @return The loaded Git repository instance.
 * @throws IOException If the repository cannot be loaded from the provided directory.
 */
fun loadRepository(repositoryDir: File): Repository {
    try {
        return FileRepositoryBuilder().apply {
            workTree = repositoryDir
        }.build()
    } catch (e: IOException) {
        logger.error(e) { "Could not load repository at path ${repositoryDir.absolutePath}: $e" }
        throw e
    }
}

/**
 * Determines the diff between two Git tags in a repository.
 *
 * @param repository The Git repository instance.
 * @param oldVersion The old version tag name.
 * @param newVersion The new version tag name.
 * @return A list of DiffEntry objects representing the changes between the two versions.
 */
fun determineDiff(repository: Repository, oldVersion: String, newVersion: String): List<DiffEntry> {
    val tags = repository.refDatabase.getRefsByPrefix(Constants.R_TAGS)
    val oldVersionRef = tags.find { tag -> tag.name == oldVersion }
    if (oldVersionRef == null) {
        logger.error { "Could not find old version ref $oldVersion" }
        return emptyList()
    }
    val oldVersionTreeId = getTreeIdForTag(repository, oldVersionRef)

    val newVersionRef = tags.find { tag -> tag.name == newVersion }
    if (newVersionRef == null) {
        logger.error { "Could not find new version ref $newVersion" }
        return emptyList()
    }
    val newVersionTreeId = getTreeIdForTag(repository, newVersionRef)

    val reader = repository.newObjectReader()
    val oldTreeIter = CanonicalTreeParser()
    oldTreeIter.reset(reader, oldVersionTreeId)
    val newTreeIter = CanonicalTreeParser()
    newTreeIter.reset(reader, newVersionTreeId)

    val diffFormatter = DiffFormatter(ByteArrayOutputStream())
    diffFormatter.setRepository(repository)
    return diffFormatter.scan(oldTreeIter, newTreeIter)
}

private fun getTreeIdForTag(repository: Repository, tag: Ref): ObjectId {
    val commitId = repository.refDatabase.peel(tag).peeledObjectId ?: tag.objectId
    val reader = repository.newObjectReader()
    val revWalk = RevWalk(reader)
    return revWalk.parseCommit(commitId).tree.id
}

/**
 * Extracts a set of file paths that have been modified or added based on the provided list of Git diff entries.
 * Entries representing deleted files are excluded from the resulting set.
 *
 * @param diffEntries The list of Git diff entries to process.
 * @return A set of file paths representing files that were modified or added.
 */
fun getChangedFilePaths(diffEntries: List<DiffEntry>): Set<String> = diffEntries
    .filter { entry -> entry.changeType != DiffEntry.ChangeType.DELETE }
    .map { diffEntry -> diffEntry.newPath }
    .toSet()

/**
 * Extracts a set of file paths that correspond to deleted or renamed files based on the provided list of Git diff entries.
 *
 * @param diffEntries The list of Git diff entries to process.
 * @return A set of file paths representing files that were deleted or renamed.
 */
fun getDeletedFilePaths(diffEntries: List<DiffEntry>): Set<String> = diffEntries
    .filter { entry -> entry.changeType == DiffEntry.ChangeType.DELETE || entry.changeType == DiffEntry.ChangeType.RENAME }
    .map { diffEntry -> diffEntry.oldPath }
    .toSet()
