package com.example.passmanagersvc.changelog

import com.example.passmanagersvc.pass.infrastructure.mongo.entity.MongoPass
import com.example.passmanagersvc.passowner.infrastructure.mongo.entity.MongoPassOwner
import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution
import org.bson.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.indexOps
import reactor.core.publisher.Flux

@ChangeUnit(id = "Create indexes", order = "1", author = "Vitalii Butriy")
internal class InitialIndexCreationChangeUnit(private val mongoTemplate: ReactiveMongoTemplate) {

    @Execution
    fun createIndexes() {
        val passOwnerIdAndPurchasedAtIndexDefinition = CompoundIndexDefinition(
            Document()
                .append(MongoPass::passOwnerId.name, 1)
                .append(MongoPass::purchasedAt.name, 1)
        )
        mongoTemplate.indexOps<MongoPass>().ensureIndex(passOwnerIdAndPurchasedAtIndexDefinition).subscribe()

        mongoTemplate.indexOps<MongoPassOwner>().apply {
            ensureIndex(Index(MongoPassOwner::email.name, ASC).unique()).subscribe()
            ensureIndex(Index(MongoPassOwner::phoneNumber.name, ASC).unique()).subscribe()
        }
    }

    @RollbackExecution
    fun dropIndexesInCaseOfFailure() {
        log.info("Rolling back ${this::class.simpleName}")

        Flux.merge(indexNamesFlux<MongoPass>(), indexNamesFlux<MongoPassOwner>())
            .filter { indexNameToCollectionNameMap.containsKey(it) }
            .flatMap { indexName ->
                val collectionName = indexNameToCollectionNameMap[indexName].orEmpty()
                mongoTemplate.indexOps(collectionName).dropIndex(indexName)
            }
            .subscribe()
    }

    private inline fun <reified T> indexNamesFlux(): Flux<String> {
        return mongoTemplate.indexOps(T::class.java).indexInfo.map { it.name }
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(InitialIndexCreationChangeUnit::class.java)
        val indexNameToCollectionNameMap: Map<String, String> = mapOf(
            "passOwnerId_1_purchasedAt_1" to MongoPass.COLLECTION_NAME,
            "email_1" to MongoPassOwner.COLLECTION_NAME,
            "phoneNumber_1" to MongoPassOwner.COLLECTION_NAME
        )
    }
}
