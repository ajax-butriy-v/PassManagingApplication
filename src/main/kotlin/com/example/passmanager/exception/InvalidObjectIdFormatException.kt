package com.example.passmanager.exception

internal class InvalidObjectIdFormatException(message: String) : RuntimeException(message) {
    constructor(idViolations: List<String>) : this("Invalid id formats: $idViolations")
}
