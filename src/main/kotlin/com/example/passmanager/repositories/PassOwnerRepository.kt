package com.example.passmanager.repositories

import com.example.passmanager.domain.MongoPassOwner

interface PassOwnerRepository {
    fun findById(passOwnerId: String): MongoPassOwner?
    fun insert(newMongoPassOwner: MongoPassOwner): MongoPassOwner
    fun deleteById(passOwnerId: String)
    fun save(newPassOwner: MongoPassOwner): MongoPassOwner
}
