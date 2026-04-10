package de.javaracing.updateDownloader.util

import de.javaracing.updateDownloader.data.AvailableVersions
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL

private val logger = KotlinLogging.logger {}

/**
 * Downloads the available version data from the specified modpack host URL.
 *
 * @param client The HTTP client used to execute the request.
 * @param modpackHostUrl The base URL of the modpack host.
 * @return A data object containing the available versions information.
 * @throws Exception If the download request fails or the response cannot be parsed.
 */
suspend fun downloadVersionData(client: OkHttpClient, modpackHostUrl: URL): AvailableVersions =
    withContext(Dispatchers.IO) {
        val versionsFileUrl = "$modpackHostUrl/versions.json"
        val request = Request.Builder()
            .url(versionsFileUrl)
            .build()

        logger.info { "Downloading version file from $versionsFileUrl" }
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Failed to download version file: ${response.code} ${response.message}")
            }

            response.body.use { responseBody ->
                Json.decodeFromString<AvailableVersions>(responseBody.string())
            }
        }
    }

