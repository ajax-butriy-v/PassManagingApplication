package com.example.passmanager.repositories.impl

import com.example.passmanager.domain.MongoPassOwner
import com.example.passmanager.repositories.PassOwnerRepository
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.remove
import org.springframework.stereotype.Repository

@Repository
class PassOwnerRepositoryImpl(private val mongoTemplate: MongoTemplate) : PassOwnerRepository {
    override fun findById(passOwnerId: String): MongoPassOwner? {
        return mongoTemplate.findById<MongoPassOwner>(passOwnerId)
    }

    override fun insert(newMongoPassOwner: MongoPassOwner): MongoPassOwner {
        return mongoTemplate.insert(newMongoPassOwner)
    }

    override fun save(modifiedMongoPassOwner: MongoPassOwner): MongoPassOwner {
        return mongoTemplate.save(modifiedMongoPassOwner)
    }

    override fun deleteById(passOwnerId: String) {
        mongoTemplate.remove<MongoPassOwner>(query(where("_id").`is`(passOwnerId)))
    }
}
