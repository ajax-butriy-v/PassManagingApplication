package com.example.passmanagersvc.repositories.impl

import com.example.passmanagersvc.domain.MongoPass
import com.example.passmanagersvc.domain.MongoPass.Companion.COLLECTION_NAME
import com.example.passmanagersvc.repositories.PassRepository
import com.example.passmanagersvc.web.dto.PriceDistribution
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
import java.math.BigDecimal
import java.time.LocalDate
import com.example.passmanagersvc.domain.MongoPassType.Companion.COLLECTION_NAME as PASS_TYPE_COLLECTION

@Suppress("TooManyFunctions")
@Repository
class PassRepositoryImpl(private val mongoTemplate: ReactiveMongoTemplate) :
    PassRepository {
    override fun findByOwnerAndPurchasedAfter(passOwnerId: String, afterDate: LocalDate): Flux<MongoPass> {
        val aggregation = newAggregation(
            getPassesByOwnerAndPurchasedAfterPipeline(passOwnerId, afterDate).operations
        )
        return mongoTemplate.aggregate<MongoPass>(aggregation, COLLECTION_NAME)
    }

    override fun findAllByPassOwnerId(passOwnerId: String): Flux<MongoPass> {
        return mongoTemplate.find<MongoPass>(query(getCriteriaByOwnerId(passOwnerId)))
    }

    override fun findById(passId: String): Mono<MongoPass> {
        return mongoTemplate.findById<MongoPass>(passId)
    }

    override fun insert(newPass: MongoPass): Mono<MongoPass> {
        return mongoTemplate.insert(newPass)
    }

    override fun save(pass: MongoPass): Mono<MongoPass> {
        return mongoTemplate.save(pass)
    }

    override fun deleteById(passId: String): Mono<Unit> {
        val query = query(where("_id").isEqualTo(passId))
        return mongoTemplate.remove<MongoPass>(query).thenReturn(Unit)
    }

    override fun deleteAllByOwnerId(passOwnerId: String): Mono<Unit> {
        val query = query(getCriteriaByOwnerId(passOwnerId))
        return mongoTemplate.remove<MongoPass>(query).thenReturn(Unit)
    }

    override fun sumPurchasedAtAfterDate(passOwnerId: String, afterDate: LocalDate): Mono<BigDecimal> {
        val aggregation = newAggregation(
            getPassesByOwnerAndPurchasedAfterPipeline(passOwnerId, afterDate)
                .add(group().sum(MongoPass::purchasedFor.name).`as`("total"))
                .add(project("total").andExclude("_id"))
                .operations
        )
        val aggregationResults = mongoTemplate.aggregate<SumResult>(aggregation, COLLECTION_NAME)
        return aggregationResults.singleOrEmpty()
            .map { it.total }
            .defaultIfEmpty(BigDecimal.ZERO)
    }

    override fun getPassesPriceDistribution(passOwnerId: String): Flux<PriceDistribution> {
        val aggregation = newAggregation(
            match(where(MongoPass::passOwnerId.name).isEqualTo(ObjectId(passOwnerId))),
            lookup().from(PASS_TYPE_COLLECTION)
                .localField(MongoPass::passTypeId.name)
                .foreignField("_id")
                .`as`("types"),
            unwind("types"),
            group("types.name").sum("types.price").`as`("spentForPassType"),
            project("spentForPassType").and("_id").`as`("typeName").andExclude("_id")
        )
        return mongoTemplate.aggregate<PriceDistribution>(aggregation, COLLECTION_NAME)
    }

    private fun getPassesByOwnerAndPurchasedAfterPipeline(passOwnerId: String, date: LocalDate): AggregationPipeline {
        return AggregationPipeline.of(
            match(getCriteriaByOwnerId(passOwnerId).andOperator(where(MongoPass::purchasedAt.name).gte(date))),
        )
    }

    private fun getCriteriaByOwnerId(passOwnerId: String): Criteria {
        return where(MongoPass::passOwnerId.name).isEqualTo(ObjectId(passOwnerId))
    }

    internal data class SumResult(val total: BigDecimal)
}
