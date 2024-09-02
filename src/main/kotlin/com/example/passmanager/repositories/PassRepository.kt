package com.example.passmanager.repositories

import com.example.passmanager.domain.MongoPass
import com.example.passmanager.domain.MongoPassOwner
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.Instant

interface PassRepository : MongoRepository<MongoPass, ObjectId> {
    fun findAllByPassOwnerAndPurchasedAtGreaterThan(mongoPassOwner: MongoPassOwner, afterDate: Instant): List<MongoPass>
    fun findAllByPassOwnerId(passOwnerId: ObjectId): List<MongoPass>
}
