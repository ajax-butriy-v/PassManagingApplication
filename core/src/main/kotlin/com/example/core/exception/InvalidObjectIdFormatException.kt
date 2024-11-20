package com.example.core.exception

class InvalidObjectIdFormatException(message: String) : RuntimeException(message) {
    constructor(idViolations: List<String>) : this("Invalid id formats: $idViolations")
}
