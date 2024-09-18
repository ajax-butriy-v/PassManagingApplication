package com.example.passmanager.repositories.impl

import com.example.passmanager.domain.MongoPassType
import com.example.passmanager.repositories.PassTypeRepository
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.remove
import org.springframework.stereotype.Repository

@Repository
class PassTypeRepositoryImpl(private val mongoTemplate: MongoTemplate) : PassTypeRepository {
    override fun findById(passOwnerId: String): MongoPassType? {
        return mongoTemplate.findById<MongoPassType>(passOwnerId)
    }

    override fun insert(newMongoPassType: MongoPassType): MongoPassType {
        return mongoTemplate.insert(newMongoPassType)
    }

    override fun save(modifiedMongoPassType: MongoPassType): MongoPassType {
        return mongoTemplate.save(modifiedMongoPassType)
    }

    override fun deleteById(passOwnerId: String) {
        mongoTemplate.remove<MongoPassType>(query(where("_id").`is`(passOwnerId)))
    }
}
