package com.example.passmanager.exception

import org.bson.types.ObjectId

class PassOwnerNotFoundException(message: String) : RuntimeException(message) {
    constructor(passOwnerId: ObjectId) : this("Could not find pass owner by id $passOwnerId")
}

