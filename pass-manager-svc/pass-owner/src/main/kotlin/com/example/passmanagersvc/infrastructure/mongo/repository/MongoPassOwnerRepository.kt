package com.example.passmanagersvc.infrastructure.mongo.repository

import com.example.passmanagersvc.application.port.out.PassOwnerRepositoryOutPort
import com.example.passmanagersvc.domain.PassOwner
import com.example.passmanagersvc.domain.PriceDistribution
import com.example.passmanagersvc.infrastructure.mongo.entity.MongoPassOwner
import com.example.passmanagersvc.infrastructure.mongo.mapper.PassOwnerMapper.toDomain
import com.example.passmanagersvc.infrastructure.mongo.mapper.PassOwnerMapper.toModel
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregate
import org.springframework.data.mongodb.core.aggregation.Aggregation.group
import org.springframework.data.mongodb.core.aggregation.Aggregation.lookup
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.project
import org.springframework.data.mongodb.core.aggregation.Aggregation.unwind
import org.springframework.data.mongodb.core.aggregation.AggregationPipeline
import org.springframework.data.mongodb.core.aggregation.Fields
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.remove
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate

@Repository
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

    override fun sumPurchasedAtAfterDate(passOwnerId: String, afterDate: LocalDate): Mono<BigDecimal> {
        val aggregation = newAggregation(
            getPassesByOwnerAndPurchasedAfterPipeline(passOwnerId, afterDate)
                .add(group().sum("purchasedFor").`as`("total"))
                .add(project("total").andExclude(Fields.UNDERSCORE_ID))
                .operations
        )
        val aggregationResults = mongoTemplate.aggregate<SumResult>(aggregation, PASS_COLLECTION)
        return aggregationResults.singleOrEmpty()
            .map { it.total }
            .defaultIfEmpty(BigDecimal.ZERO)
    }

    override fun getPassesPriceDistribution(passOwnerId: String): Flux<PriceDistribution> {
        val aggregation = newAggregation(
            match(where("passOwnerId").isEqualTo(ObjectId(passOwnerId))),
            lookup().from("pass_type")
                .localField("passTypeId")
                .foreignField("_id")
                .`as`("types"),
            unwind("types"),
            group("types.name").sum("types.price").`as`("spentForPassType"),
            project("spentForPassType").and("_id").`as`("typeName").andExclude("_id")
        )
        return mongoTemplate.aggregate<PriceDistribution>(aggregation, PASS_COLLECTION)
    }

    private fun getPassesByOwnerAndPurchasedAfterPipeline(passOwnerId: String, date: LocalDate): AggregationPipeline {
        return AggregationPipeline.of(
            match(getCriteriaByOwnerId(passOwnerId).andOperator(where("purchasedAt").gte(date))),
        )
    }

    private fun getCriteriaByOwnerId(passOwnerId: String): Criteria {
        return where("passOwnerId").isEqualTo(ObjectId(passOwnerId))
    }

    internal data class SumResult(val total: BigDecimal)

    companion object {
        const val PASS_COLLECTION = "pass"
    }
}
