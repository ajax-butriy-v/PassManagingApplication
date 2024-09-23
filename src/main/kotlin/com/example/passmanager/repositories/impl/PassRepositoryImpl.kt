package com.example.passmanager.repositories.impl

import com.example.passmanager.domain.MongoPass
import com.example.passmanager.domain.MongoPass.Companion.COLLECTION_NAME
import com.example.passmanager.repositories.PassRepository
import com.example.passmanager.web.dto.PriceDistribution
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregate
import org.springframework.data.mongodb.core.aggregation.Aggregation.group
import org.springframework.data.mongodb.core.aggregation.Aggregation.lookup
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.project
import org.springframework.data.mongodb.core.aggregation.Aggregation.unwind
import org.springframework.data.mongodb.core.aggregation.AggregationPipeline
import org.springframework.data.mongodb.core.aggregation.LookupOperation
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.remove
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDate
import com.example.passmanager.domain.MongoPassOwner.Companion.COLLECTION_NAME as PASS_OWNER_COLLECTION
import com.example.passmanager.domain.MongoPassType.Companion.COLLECTION_NAME as PASS_TYPE_COLLECTION

@Suppress("TooManyFunctions")
@Repository
class PassRepositoryImpl(private val mongoTemplate: MongoTemplate) : PassRepository {
    override fun findByOwnerAndPurchasedAfter(passOwnerId: String, afterDate: LocalDate): List<MongoPass> {
        val aggregation = newAggregation(
            getPassesByOwnerAndPurchasedAfterPipeline(passOwnerId, afterDate).operations
        )
        return mongoTemplate.aggregate<MongoPass>(aggregation, COLLECTION_NAME).mappedResults
    }

    override fun findAllByPassOwnerId(passOwnerId: String): List<MongoPass> {
        val pipelineStages = getPassesLookUpAggregationPipeline(passOwnerId).operations
        val aggregationResults =
            mongoTemplate.aggregate<ProjectionResult>(
                newAggregation(pipelineStages),
                PASS_OWNER_COLLECTION
            )
        return aggregationResults.uniqueMappedResult?.passes.orEmpty()
    }

    override fun findById(passId: String): MongoPass? {
        return mongoTemplate.findById<MongoPass>(passId)
    }

    override fun insert(newPass: MongoPass): MongoPass {
        return mongoTemplate.insert(newPass)
    }

    override fun save(pass: MongoPass): MongoPass {
        return mongoTemplate.save(pass)
    }

    override fun deleteById(passId: String) {
        mongoTemplate.remove<MongoPass>(query(where("_id").isEqualTo(passId)))
    }

    override fun deleteAllByOwnerId(passOwnerId: String) {
        mongoTemplate.remove<MongoPass>(query(where(MongoPass::passOwnerId.name).isEqualTo(ObjectId(passOwnerId))))
    }

    override fun sumPurchasedAtAfterDate(passOwnerId: String, afterDate: LocalDate): BigDecimal {
        val aggregation = newAggregation(
            getPassesByOwnerAndPurchasedAfterPipeline(passOwnerId, afterDate)
                .add(group().sum(MongoPass::purchasedFor.name).`as`("total"))
                .add(project("total").andExclude("_id"))
                .operations
        )
        val aggregationResults = mongoTemplate.aggregate<SumResult>(aggregation, COLLECTION_NAME)
        return aggregationResults.uniqueMappedResult?.total ?: BigDecimal.ZERO
    }

    override fun getPassesPriceDistribution(passOwnerId: String): List<PriceDistribution> {
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
        val aggregationResults = mongoTemplate.aggregate<PriceDistribution>(aggregation, COLLECTION_NAME)
        return aggregationResults.mappedResults
    }

    private fun getPassesByOwnerAndPurchasedAfterPipeline(passOwnerId: String, date: LocalDate): AggregationPipeline {
        return AggregationPipeline.of(
            match(
                where(MongoPass::passOwnerId.name).isEqualTo(ObjectId(passOwnerId))
                    .andOperator(where(MongoPass::purchasedAt.name).gte(date))
            ),
        )
    }

    private fun getPassesLookUpAggregationPipeline(passOwnerId: String): AggregationPipeline {
        val lookupOperation = LookupOperation.newLookup()
            .from(COLLECTION_NAME)
            .localField("_id")
            .foreignField(MongoPass::passOwnerId.name)
            .`as`("passes")
        return AggregationPipeline.of(
            match(where("_id").isEqualTo(passOwnerId)),
            lookupOperation,
            project("passes").andExclude("_id")
        )
    }

    internal data class ProjectionResult(val passes: List<MongoPass>)

    internal data class SumResult(val total: BigDecimal)
}
