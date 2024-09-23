package com.example.passmanager.service

import com.example.passmanager.domain.MongoPassType

interface PassTypeService {
    fun findById(id: String): MongoPassType?
    fun getById(id: String): MongoPassType
    fun create(passType: MongoPassType): MongoPassType
    fun update(modifiedPassType: MongoPassType): MongoPassType
    fun deleteById(id: String)
}
