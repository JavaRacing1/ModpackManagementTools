package de.javaracing.updateDownloader.util

import java.io.File
import java.security.MessageDigest

private const val FILE_BUFFER_SIZE = 8192

/**
 * Computes the cryptographic hash of the file using the specified algorithm.
 *
 * @param file The file for which the hash should be calculated.
 * @param algorithm The name of the hashing algorithm to use (e.g. "SHA-256"). Defaults to "SHA-256" if not specified.
 * @return The computed hash as a hexadecimal string.
 */
fun calculateHash(file: File, algorithm: String = "SHA-256"): String {
    val digest = MessageDigest.getInstance(algorithm)
    file.inputStream().use { inputStream ->
        val buffer = ByteArray(FILE_BUFFER_SIZE)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            digest.update(buffer, 0, bytesRead)
        }
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
}
