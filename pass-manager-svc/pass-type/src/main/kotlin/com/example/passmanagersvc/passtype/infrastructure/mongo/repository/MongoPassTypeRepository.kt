package com.example.passmanagersvc.passtype.infrastructure.mongo.repository

import com.example.passmanagersvc.passtype.application.port.output.PassTypeRepositoryOutPort
import com.example.passmanagersvc.passtype.domain.PassType
import com.example.passmanagersvc.passtype.infrastructure.mongo.entity.MongoPassType
import com.example.passmanagersvc.passtype.infrastructure.mongo.mapper.PassTypeMapper.toDomain
import com.example.passmanagersvc.passtype.infrastructure.mongo.mapper.PassTypeMapper.toModel
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
    override fun findById(passTypeId: String): Mono<PassType> {
        return mongoTemplate.findById<MongoPassType>(passTypeId)
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

    override fun deleteById(passTypeId: String): Mono<Unit> {
        val query = query(where(Fields.UNDERSCORE_ID).isEqualTo(passTypeId))
        return mongoTemplate.remove<MongoPassType>(query).thenReturn(Unit)
    }
}
