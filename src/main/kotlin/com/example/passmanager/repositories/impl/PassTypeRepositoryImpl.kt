package com.example.passmanager.repositories.impl

import com.example.passmanager.domain.MongoPassType
import com.example.passmanager.repositories.PassTypeRepository
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.remove
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class PassTypeRepositoryImpl(private val mongoTemplate: ReactiveMongoTemplate) : PassTypeRepository {
    override fun findById(passOwnerId: String): Mono<MongoPassType> {
        return mongoTemplate.findById<MongoPassType>(passOwnerId)
    }

    override fun insert(newMongoPassType: MongoPassType): Mono<MongoPassType> {
        return mongoTemplate.insert(newMongoPassType)
    }

    override fun save(modifiedMongoPassType: MongoPassType): Mono<MongoPassType> {
        return mongoTemplate.save(modifiedMongoPassType)
    }

    override fun deleteById(passOwnerId: String): Mono<Unit> {
        val query = query(where("_id").isEqualTo(passOwnerId))
        return mongoTemplate.remove<MongoPassType>(query).thenReturn(Unit)
    }
}
