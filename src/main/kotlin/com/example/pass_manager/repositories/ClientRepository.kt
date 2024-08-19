package com.example.pass_manager.repositories

import com.example.pass_manager.domain.MongoClient
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface ClientRepository : MongoRepository<MongoClient, ObjectId> {
    fun existsByEmailOrPhoneNumber(email: String, phoneNumber: String): Boolean
    fun findByEmailAndPhoneNumber(email: String, phoneNumber: String): MongoClient?
}