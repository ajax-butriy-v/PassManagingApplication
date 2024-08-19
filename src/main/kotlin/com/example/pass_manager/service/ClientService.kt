package com.example.pass_manager.service

import com.example.pass_manager.domain.MongoClient
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.Instant

interface ClientService {
    fun findById(clientId: ObjectId): MongoClient?
    fun create(newClient: MongoClient): MongoClient
    fun update(clientId: ObjectId, modifiedClient: MongoClient): MongoClient
    fun cancelPass(clientId: ObjectId, passId: ObjectId): Boolean
    fun calculateSpentAfterDate(afterDate: Instant, clientId: ObjectId): BigDecimal
    fun deleteById(clientId: ObjectId)
}