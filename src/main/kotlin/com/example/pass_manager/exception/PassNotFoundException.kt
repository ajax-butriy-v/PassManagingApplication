package com.example.pass_manager.exception

import org.bson.types.ObjectId

class PassNotFoundException(message: String) : RuntimeException(message) {
    constructor(passId: ObjectId) : this("Could not find pass by id $passId")

}