package de.javaracing.update_assembler.exception

import java.io.Serial

class TagNotFoundException(message: String) : Exception(message) {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -7217205834024751617L
    }
}