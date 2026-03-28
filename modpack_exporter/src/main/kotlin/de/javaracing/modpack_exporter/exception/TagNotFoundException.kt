package de.javaracing.modpack_exporter.exception

import java.io.Serial

class TagNotFoundException(val tagName: String, message: String) : Exception(message) {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -7217205834024751617L
    }
}
