package com.example.pass_manager.exception

import org.bson.types.ObjectId

class ClientNotFoundException(message: String) : RuntimeException(message) {
    constructor(clientId: ObjectId) : this("Could not find client by id $clientId")
}

