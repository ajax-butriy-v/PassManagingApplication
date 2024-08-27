package com.example.pass_manager.repositories

import com.example.pass_manager.domain.MongoPass
import com.example.pass_manager.domain.MongoPassOwner
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.Update
import java.time.Instant

interface PassRepository : MongoRepository<MongoPass, ObjectId> {
    fun findAllByPassOwnerAndPurchasedAtGreaterThan(mongoPassOwner: MongoPassOwner, afterDate: Instant): List<MongoPass>
    fun findAllByPassOwnerId(passOwnerId: ObjectId): List<MongoPass>

    @Query("{ '_id': ?0 }")
    @Update("{ '\$set': { 'passOwner': ?1 } }")
    fun updateMongoPassByPassOwner(pass: MongoPass, passOwner: MongoPassOwner)
}

