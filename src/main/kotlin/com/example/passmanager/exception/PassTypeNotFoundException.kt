package com.example.passmanager.exception

import org.bson.types.ObjectId

class PassTypeNotFoundException(message: String) : RuntimeException(message) {
    constructor(passOwnerId: ObjectId) : this("Could not find pass type by id $passOwnerId")
}
