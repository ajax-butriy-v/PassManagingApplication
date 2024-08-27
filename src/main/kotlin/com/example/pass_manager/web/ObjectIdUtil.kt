package com.example.pass_manager.web

import org.bson.types.ObjectId

/** Was created to pass ObjectId from String to service layer from controllers
 * (because MongoRepository requires ObjectId). Will be removed in future,
 * when MongoRepository will be implemented using MongoTemplate (which accepts
 * id as String to be used in queries). */
fun String.toObjectId(): ObjectId = ObjectId(this)

