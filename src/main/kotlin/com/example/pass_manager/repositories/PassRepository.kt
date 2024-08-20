package com.example.pass_manager.repositories

import com.example.pass_manager.domain.Client
import com.example.pass_manager.domain.MongoPass
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.Instant

interface PassRepository : MongoRepository<MongoPass, ObjectId> {
    fun findAllByClientAndPurchasedAtAfter(client: Client, afterDate: Instant): List<MongoPass>
    fun findAllByClientId(clientId: ObjectId): List<MongoPass>
    fun updateMongoPassByClient(pass: MongoPass, client: Client)
}
