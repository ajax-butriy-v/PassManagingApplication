package com.example.passmanager.exception

class InvalidIdTypeException(message: String) : RuntimeException(message) {
    constructor(idViolations: List<String>) : this("Invalid id formats: $idViolations")
}
