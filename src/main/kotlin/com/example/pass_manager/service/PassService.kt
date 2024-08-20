package com.example.pass_manager.service

import com.example.pass_manager.domain.MongoPass
import com.example.pass_manager.web.dto.PriceDistribution
import org.bson.types.ObjectId

interface PassService {
    fun findById(passId: ObjectId): MongoPass?
    fun create(newPass: MongoPass): MongoPass
    fun deleteById(passId: ObjectId)
    fun calculatePriceDistribution(clientId: ObjectId): List<PriceDistribution>
    fun transferPassToAnotherClient(passId: ObjectId, targetClientId: ObjectId)
}

