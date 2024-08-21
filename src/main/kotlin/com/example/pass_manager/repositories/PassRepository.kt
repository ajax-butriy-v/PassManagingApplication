package com.example.pass_manager.repositories

import com.example.pass_manager.domain.MongoPass
import com.example.pass_manager.domain.MongoPassOwner
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.Instant

interface PassRepository : MongoRepository<MongoPass, ObjectId> {
    fun findAllByPassOwnerAndPurchasedAtAfter(mongoPassOwner: MongoPassOwner, afterDate: Instant): List<MongoPass>
    fun findAllByPassOwnerId(passOwnerId: ObjectId): List<MongoPass>
    fun updateMongoPassByPassOwner(pass: MongoPass, passOwner: MongoPassOwner): MongoPass
}

