package com.example.passmanagersvc.infrastructure.mongo.repository

import com.example.passmanagersvc.application.port.out.PassOwnerRepositoryOutPort
import com.example.passmanagersvc.domain.PassOwner
import com.example.passmanagersvc.infrastructure.mongo.entity.MongoPassOwner
import com.example.passmanagersvc.infrastructure.mongo.mapper.PassOwnerMapper.toDomain
import com.example.passmanagersvc.infrastructure.mongo.mapper.PassOwnerMapper.toModel
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Fields
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.remove
import reactor.core.publisher.Mono

class MongoPassOwnerRepository(private val mongoTemplate: ReactiveMongoTemplate) : PassOwnerRepositoryOutPort {
    override fun findById(passOwnerId: String): Mono<PassOwner> {
        return mongoTemplate.findById<MongoPassOwner>(passOwnerId)
            .map { it.toDomain() }
    }

    override fun insert(newPassOwner: PassOwner): Mono<PassOwner> {
        return mongoTemplate.insert(newPassOwner.toModel())
            .map { it.toDomain() }
    }

    override fun deleteById(passOwnerId: String): Mono<Unit> {
        val query = query(where(Fields.UNDERSCORE_ID).isEqualTo(passOwnerId))
        return mongoTemplate.remove<MongoPassOwner>(query).thenReturn(Unit)
    }

    override fun save(newPassOwner: PassOwner): Mono<PassOwner> {
        return mongoTemplate.save(newPassOwner.toModel())
            .map { it.toDomain() }
    }
}