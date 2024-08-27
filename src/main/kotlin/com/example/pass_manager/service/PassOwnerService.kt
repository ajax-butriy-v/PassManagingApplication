package com.example.pass_manager.service

import com.example.pass_manager.domain.MongoPassOwner
import org.bson.types.ObjectId

interface PassOwnerService {
    fun findById(passOwnerId: ObjectId): MongoPassOwner?
    fun create(newMongoPassOwner: MongoPassOwner): MongoPassOwner
    fun update(passOwnerId: ObjectId, modifiedMongoPassOwner: MongoPassOwner): MongoPassOwner
    fun deleteById(passOwnerId: ObjectId)
}

