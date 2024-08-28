package com.example.passmanager.repositories

import com.example.passmanager.domain.MongoPassOwner
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface PassOwnerRepository : MongoRepository<MongoPassOwner, ObjectId>

