package com.example.pass_manager.repositories

import com.example.pass_manager.domain.Client
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface ClientRepository : MongoRepository<Client, ObjectId> {
    fun existsByEmailOrPhoneNumber(email: String, phoneNumber: String): Boolean
    fun findByEmailAndPhoneNumber(email: String, phoneNumber: String): Client?
}
