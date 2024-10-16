package com.example.gateway.exception

class InvalidObjectIdFormatException(message: String) : RuntimeException(message) {
    constructor(idViolations: List<String>) : this("Invalid id formats: $idViolations")
}
