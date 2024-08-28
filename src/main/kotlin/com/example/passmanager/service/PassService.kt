package com.example.passmanager.service

import com.example.passmanager.domain.MongoPass
import com.example.passmanager.domain.MongoPassOwner
import org.bson.types.ObjectId
import java.time.Instant

interface PassService {
    fun findById(passId: ObjectId): MongoPass?
    fun create(newPass: MongoPass, ownerId: ObjectId, passTypeId: ObjectId): MongoPass
    fun deleteById(passId: ObjectId)
    fun findAllByPassOwnerAndPurchasedAtGreaterThan(passOwner: MongoPassOwner, afterDate: Instant): List<MongoPass>
    fun findAllByPassOwnerId(passOwnerId: ObjectId): List<MongoPass>
    fun updateByPassOwner(pass: MongoPass, passOwner: MongoPassOwner)
}

