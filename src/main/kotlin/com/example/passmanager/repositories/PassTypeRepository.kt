package com.example.passmanager.repositories

import com.example.passmanager.domain.MongoPassType
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface PassTypeRepository : MongoRepository<MongoPassType, ObjectId>
