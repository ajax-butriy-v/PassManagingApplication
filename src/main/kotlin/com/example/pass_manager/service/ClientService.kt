package com.example.pass_manager.service

import com.example.pass_manager.domain.Client
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.Instant

interface ClientService {
    fun findById(clientId: ObjectId): Client?
    fun create(newClient: Client): Client
    fun update(clientId: ObjectId, modifiedClient: Client): Client
    fun cancelPass(clientId: ObjectId, passId: ObjectId): Boolean
    fun calculateSpentAfterDate(afterDate: Instant, clientId: ObjectId): BigDecimal
    fun deleteById(clientId: ObjectId)
}
