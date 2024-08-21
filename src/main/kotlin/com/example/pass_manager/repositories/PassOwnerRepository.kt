package com.example.pass_manager.repositories

import com.example.pass_manager.domain.MongoPassOwner
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface PassOwnerRepository : MongoRepository<MongoPassOwner, ObjectId>

