package com.example.passmanager.service

import com.example.passmanager.domain.MongoPassOwner
import org.bson.types.ObjectId

interface PassOwnerService {
    fun findById(passOwnerId: ObjectId): MongoPassOwner?
    fun getById(passOwnerId: ObjectId): MongoPassOwner
    fun create(newMongoPassOwner: MongoPassOwner): MongoPassOwner
    fun update(passOwnerId: ObjectId, modifiedMongoPassOwner: MongoPassOwner): MongoPassOwner
    fun deleteById(passOwnerId: ObjectId)
}
