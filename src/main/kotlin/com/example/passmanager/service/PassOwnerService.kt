package com.example.passmanager.service

import com.example.passmanager.domain.MongoPassOwner

interface PassOwnerService {
    fun findById(passOwnerId: String): MongoPassOwner?
    fun getById(passOwnerId: String): MongoPassOwner
    fun create(newMongoPassOwner: MongoPassOwner): MongoPassOwner
    fun update(newPassOwner: MongoPassOwner): MongoPassOwner
    fun deleteById(passOwnerId: String)
}
