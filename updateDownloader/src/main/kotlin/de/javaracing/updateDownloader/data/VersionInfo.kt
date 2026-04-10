package de.javaracing.updateDownloader.data

import kotlinx.serialization.Serializable

@Serializable
data class VersionInfo(val version: String, val downloadUrl: String, val releasedAt: String)

@Serializable
data class AvailableVersions(val versions: List<VersionInfo>)
