package com.example.passmanagersvc.infrastructure.mongo.repository

import com.example.passmanagersvc.application.port.out.PassTypeRepositoryOutPort
import com.example.passmanagersvc.domain.PassType
import com.example.passmanagersvc.infrastructure.mongo.entity.MongoPassType
import com.example.passmanagersvc.infrastructure.mongo.mapper.PassTypeMapper.toDomain
import com.example.passmanagersvc.infrastructure.mongo.mapper.PassTypeMapper.toModel
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Fields
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.remove
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class MongoPassTypeRepository(private val mongoTemplate: ReactiveMongoTemplate) : PassTypeRepositoryOutPort {
    override fun findById(passOwnerId: String): Mono<PassType> {
        return mongoTemplate.findById<MongoPassType>(passOwnerId)
            .map { it.toDomain() }
    }

    override fun insert(newPassType: PassType): Mono<PassType> {
        return mongoTemplate.insert(newPassType.toModel())
            .map { it.toDomain() }
    }

    override fun save(modifiedPassType: PassType): Mono<PassType> {
        return mongoTemplate.save(modifiedPassType.toModel())
            .map { it.toDomain() }
    }

    override fun deleteById(passOwnerId: String): Mono<Unit> {
        val query = query(where(Fields.UNDERSCORE_ID).isEqualTo(passOwnerId))
        return mongoTemplate.remove<MongoPassType>(query).thenReturn(Unit)
    }
}
