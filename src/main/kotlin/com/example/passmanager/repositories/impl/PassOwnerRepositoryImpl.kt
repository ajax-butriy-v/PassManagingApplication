package com.example.passmanager.repositories.impl

import com.example.passmanager.domain.MongoPassOwner
import com.example.passmanager.repositories.PassOwnerRepository
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.remove
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class PassOwnerRepositoryImpl(private val mongoTemplate: ReactiveMongoTemplate) : PassOwnerRepository {
    override fun findById(passOwnerId: String): Mono<MongoPassOwner> {
        return mongoTemplate.findById(passOwnerId)
    }

    override fun insert(newMongoPassOwner: MongoPassOwner): Mono<MongoPassOwner> {
        return mongoTemplate.insert(newMongoPassOwner)
    }

    override fun deleteById(passOwnerId: String): Mono<Unit> {
        val query = query(where("_id").isEqualTo(passOwnerId))
        return mongoTemplate.remove<MongoPassOwner>(query).thenReturn(Unit)
    }

    override fun save(newPassOwner: MongoPassOwner): Mono<MongoPassOwner> {
        return mongoTemplate.save(newPassOwner)
    }
}
