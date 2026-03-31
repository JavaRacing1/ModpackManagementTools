package de.javaracing.modpackExporter.util

import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.pathString

private const val FILE_BUFFER_SIZE = 8192

/**
 * Computes the cryptographic hash of the file at the given path using the specified algorithm.
 *
 * @param path The path to the file for which the hash should be calculated.
 * @param algorithm The name of the hashing algorithm to use (e.g. "SHA-256"). Defaults to "SHA-256" if not specified.
 * @return The computed hash as a hexadecimal string.
 */
fun calculateHash(path: Path, algorithm: String = "SHA-256"): String {
    val digest = MessageDigest.getInstance(algorithm)
    path.toFile().inputStream().use { inputStream ->
        val buffer = ByteArray(FILE_BUFFER_SIZE)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            digest.update(buffer, 0, bytesRead)
        }
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
}

/**
 * Calculates the cryptographic hash of a file and saves it to a new file.
 * The hash is computed using the specified algorithm and stored in a file
 * with the same name as the original file, appended by the name of the algorithm.
 *
 * @param path The path to the file for which the hash should be calculated.
 * @param algorithm The name of the hashing algorithm to use (e.g. "SHA-256"). Defaults to "SHA-256" if not specified.
 * @return The computed hash as a hexadecimal string.
 */
fun calculateHashAndSave(path: Path, algorithm: String = "SHA-256"): String {
    val hash = calculateHash(path, algorithm)
    val hashFile = Path(path.pathString + ".${algorithm.lowercase().replace("-", "")}")
    hashFile.toFile().writeText(hash)
    return hash
}
