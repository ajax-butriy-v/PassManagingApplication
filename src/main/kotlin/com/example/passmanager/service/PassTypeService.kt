package com.example.passmanager.service

import com.example.passmanager.domain.MongoPassType
import org.bson.types.ObjectId

interface PassTypeService {
    fun findById(id: ObjectId): MongoPassType?
    fun getById(id: ObjectId): MongoPassType
    fun create(passType: MongoPassType): MongoPassType
    fun deleteById(id: ObjectId)
}

