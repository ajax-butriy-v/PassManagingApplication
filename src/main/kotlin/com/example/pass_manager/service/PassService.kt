package com.example.pass_manager.service

import com.example.pass_manager.domain.MongoPass
import com.example.pass_manager.domain.MongoPassOwner
import org.bson.types.ObjectId
import java.time.Instant

interface PassService {
    fun findById(passId: ObjectId): MongoPass?
    fun create(newPass: MongoPass): MongoPass
    fun deleteById(passId: ObjectId)
    fun findAllByPassOwnerAndPurchasedAtAfter(passOwner: MongoPassOwner, afterDate: Instant): List<MongoPass>
    fun findAllByPassOwnerId(passOwnerId: ObjectId): List<MongoPass>
    fun updateByPassOwner(pass: MongoPass, passOwner: MongoPassOwner)
}

