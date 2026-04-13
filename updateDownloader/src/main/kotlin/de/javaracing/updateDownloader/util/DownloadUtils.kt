package de.javaracing.updateDownloader.util

import de.javaracing.updateDownloader.data.AvailableVersions
import de.javaracing.updateDownloader.data.VersionInfo
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.net.URL
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

/**
 * Downloads the available version data from the specified modpack host URL.
 *
 * @param client The HTTP client used to execute the request.
 * @param versionDataUrl The URL of the version data file.
 * @return A data object containing the available versions information.
 * @throws Exception If the download request fails or the response cannot be parsed.
 */
suspend fun downloadVersionData(client: OkHttpClient, versionDataUrl: URL): AvailableVersions =
    withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(versionDataUrl)
            .build()

        logger.info { "Downloading version file from $versionDataUrl" }
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Failed to download version file: ${response.code} ${response.message}")
            }

            response.body.use { responseBody ->
                Json.decodeFromString<AvailableVersions>(responseBody.string())
            }
        }
    }

/**
 * Downloads an update archive from the specified URL and saves it to a temporary directory.
 *
 * @param client The HTTP client used to execute the request.
 * @param versionInfo Information about the update, including version and download URL.
 * @param tempDir The directory where the downloaded update file will be temporarily saved.
 * @return The downloaded update file.
 * @throws Exception if the update download fails
 */
suspend fun downloadUpdate(client: OkHttpClient, versionInfo: VersionInfo, tempDir: Path): File =
    withContext(Dispatchers.IO) {
        val downloadUrl = versionInfo.downloadUrl
        val version = versionInfo.version
        val request = Request.Builder()
            .url(downloadUrl)
            .build()

        logger.info { "Downloading update $version from $downloadUrl" }
        val fileExtension = downloadUrl.substringBefore('?').substringAfterLast('.', ".zip")
        val outputFile = tempDir.resolve("update_$version.$fileExtension").toFile()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Failed to download update $version: ${response.code} ${response.message}")
            }

            response.body.use { responseBody ->
                responseBody.byteStream().use { input ->
                    outputFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }

        if (versionInfo.isVerificationEnabled) {
            val sha256 = getSha256ForVersion(client, versionInfo)
            logger.info { "SHA256 for update $version: $sha256" }
            val calculatedSha256 = calculateHash(outputFile)
            if (calculatedSha256 != sha256) {
                throw IOException("SHA256 mismatch for update $version: expected $sha256, got $calculatedSha256")
            }
        }

        outputFile
    }

/**
 * Downloads all updates specified in the given list and saves them to the specified temporary directory.
 * The download tasks are executed in parallel with a limit on the maximum number of concurrent downloads.
 *
 * @param client The HTTP client used to execute the download requests.
 * @param versions A list of version information objects, each containing details about the update to be downloaded.
 * @param tempDir The temporary directory where all downloaded update files will be saved.
 * @param maxParallelDownloads The maximum number of concurrent downloads allowed.
 * @return A map where the keys are version strings and the values are the corresponding downloaded update files.
 */
suspend fun downloadAllUpdates(
    client: OkHttpClient,
    versions: List<VersionInfo>,
    tempDir: Path,
    maxParallelDownloads: Int
): Map<String, File> = coroutineScope {
    val semaphore = Semaphore(maxParallelDownloads)

    versions.map { versionInfo ->
        async(Dispatchers.IO) {
            semaphore.withPermit {
                versionInfo.version to downloadUpdate(client, versionInfo, tempDir)
            }
        }
    }.awaitAll().toMap()
}

private suspend fun getSha256ForVersion(client: OkHttpClient, versionInfo: VersionInfo): String {
    if (versionInfo.sha256.isNotBlank()) {
        return versionInfo.sha256
    }

    return withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(versionInfo.sha256Url)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Failed to download SHA256 for update ${versionInfo.version}: ${response.code} ${response.message}")
            }

            response.body.use { responseBody ->
                responseBody.string()
            }
        }
    }
}
