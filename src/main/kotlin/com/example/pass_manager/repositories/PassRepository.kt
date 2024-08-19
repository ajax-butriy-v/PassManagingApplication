package com.example.pass_manager.repositories

import com.example.pass_manager.domain.MongoClient
import com.example.pass_manager.domain.MongoPass
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.Update
import java.time.Instant

interface PassRepository : MongoRepository<MongoPass, ObjectId> {
    fun findAllByClientAndPurchasedAtAfter(client: MongoClient, afterDate: Instant): List<MongoPass>
    fun findAllByClientId(clientId: ObjectId): List<MongoPass>

    @Query("{ '_id': ?0 }")
    @Update("{ '\$set': { 'client': ?1 } }")
    fun updateMongoPassByClient(pass: MongoPass, client: MongoClient)
}