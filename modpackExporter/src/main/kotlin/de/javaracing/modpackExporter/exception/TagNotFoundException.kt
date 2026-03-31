package de.javaracing.modpackExporter.exception

import java.io.Serial

/**
 * Exception thrown when a specific tag is not found in a Git repository.
 */
class TagNotFoundException(val tagName: String, message: String) : Exception(message) {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 1L
    }
}
