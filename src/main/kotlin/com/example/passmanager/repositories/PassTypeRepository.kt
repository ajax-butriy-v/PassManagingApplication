package com.example.passmanager.repositories

import com.example.passmanager.domain.MongoPassType

interface PassTypeRepository {
    fun findById(passOwnerId: String): MongoPassType?
    fun insert(newMongoPassType: MongoPassType): MongoPassType
    fun save(modifiedMongoPassType: MongoPassType): MongoPassType
    fun deleteById(passOwnerId: String)
}
