package com.example.passmanagersvc.pass.infrastructure.mongo.repository

import com.example.passmanagersvc.pass.application.port.output.PassRepositoryOutPort
import com.example.passmanagersvc.pass.domain.Pass
import com.example.passmanagersvc.pass.infrastructure.mongo.entity.MongoPass
import com.example.passmanagersvc.pass.infrastructure.mongo.entity.MongoPass.Companion.COLLECTION_NAME
import com.example.passmanagersvc.pass.infrastructure.mongo.mapper.PassMapper.toDomain
import com.example.passmanagersvc.pass.infrastructure.mongo.mapper.PassMapper.toModel
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregate
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation
import org.springframework.data.mongodb.core.aggregation.AggregationPipeline
import org.springframework.data.mongodb.core.aggregation.Fields
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.remove
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

@Repository
class MongoPassRepository(private val mongoTemplate: ReactiveMongoTemplate) : PassRepositoryOutPort {
    override fun findByOwnerAndPurchasedAfter(passOwnerId: String, afterDate: LocalDate): Flux<Pass> {
        val aggregation = newAggregation(
            getPassesByOwnerAndPurchasedAfterPipeline(passOwnerId, afterDate).operations
        )
        return mongoTemplate.aggregate<MongoPass>(aggregation, COLLECTION_NAME)
            .map { it.toDomain() }
    }

    override fun findAllByPassOwnerId(passOwnerId: String): Flux<Pass> {
        return mongoTemplate.find<MongoPass>(query(getCriteriaByOwnerId(passOwnerId)))
            .map { it.toDomain() }
    }

    override fun findById(passId: String): Mono<Pass> {
        return mongoTemplate.findById<MongoPass>(passId)
            .map { it.toDomain() }
    }

    override fun insert(newPass: Pass): Mono<Pass> {
        return mongoTemplate.insert<MongoPass>(newPass.toModel())
            .map { it.toDomain() }
    }

    override fun save(pass: Pass): Mono<Pass> {
        return mongoTemplate.save(pass.toModel())
            .map { it.toDomain() }
    }

    override fun deleteById(passId: String): Mono<Unit> {
        val query = query(where(Fields.UNDERSCORE_ID).isEqualTo(passId))
        return mongoTemplate.remove<MongoPass>(query).thenReturn(Unit)
    }

    override fun deleteAllByOwnerId(passOwnerId: String): Mono<Unit> {
        val query = query(getCriteriaByOwnerId(passOwnerId))
        return mongoTemplate.remove<MongoPass>(query).thenReturn(Unit)
    }

    private fun getPassesByOwnerAndPurchasedAfterPipeline(passOwnerId: String, date: LocalDate): AggregationPipeline {
        return AggregationPipeline.of(
            match(getCriteriaByOwnerId(passOwnerId).andOperator(where(MongoPass::purchasedAt.name).gte(date))),
        )
    }

    private fun getCriteriaByOwnerId(passOwnerId: String): Criteria {
        return where(MongoPass::passOwnerId.name).isEqualTo(ObjectId(passOwnerId))
    }
}
