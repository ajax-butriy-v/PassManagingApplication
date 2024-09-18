package com.example.passmanager.service

import com.example.passmanager.domain.MongoPass
import java.time.LocalDate

interface PassService {
    fun findById(passId: String): MongoPass?
    fun getById(passId: String): MongoPass
    fun create(newPass: MongoPass, ownerId: String, passTypeId: String): MongoPass
    fun update(pass: MongoPass): MongoPass
    fun deleteById(passId: String)
    fun deleteAllByOwnerId(passOwnerId: String)
    fun findAllByPassOwnerAndPurchasedAtGreaterThan(passOwnerId: String, afterDate: LocalDate): List<MongoPass>
    fun findAllByPassOwnerId(passOwnerId: String): List<MongoPass>
}
