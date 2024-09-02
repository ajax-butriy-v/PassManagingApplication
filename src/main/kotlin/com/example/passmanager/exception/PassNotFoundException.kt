package com.example.passmanager.exception

import org.bson.types.ObjectId

internal class PassNotFoundException(message: String) : RuntimeException(message) {
    constructor(passId: ObjectId) : this("Could not find pass by id $passId")
}
