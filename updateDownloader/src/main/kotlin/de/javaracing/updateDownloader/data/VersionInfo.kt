package de.javaracing.updateDownloader.data

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class VersionInfo(val version: String, val downloadUrl: String, val releasedAt: Instant)

@Serializable
data class AvailableVersions(private val versions: List<VersionInfo>) {

    /**
     * Retrieves a list of versions that have been released after the specified current version.
     *
     * @param currentVersion The version string representing the current version to compare against.
     * @return A list of `VersionInfo` objects representing the versions released after the current version.
     * @throws IllegalArgumentException If the specified current version is not found in the available versions.
     */
    fun getNewerVersions(currentVersion: String): List<VersionInfo> {
        val currentVersionReleasedAt = versions.find { it.version == currentVersion }?.releasedAt
            ?: throw IllegalArgumentException("Current version $currentVersion not found")
        return versions.filter { it.releasedAt > currentVersionReleasedAt }.sortedBy { it.releasedAt }
    }
}
