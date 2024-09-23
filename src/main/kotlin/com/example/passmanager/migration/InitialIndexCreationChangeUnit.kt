package com.example.passmanager.migration

import com.example.passmanager.domain.MongoPass
import com.example.passmanager.domain.MongoPassOwner
import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution
import org.bson.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.indexOps

@ChangeUnit(id = "Create indexes", order = "1", author = "Vitalii Butriy")
internal class InitialIndexCreationChangeUnit(private val mongoTemplate: MongoTemplate) {
    private lateinit var passOwnerIdAndPurchasedAtIndexName: String
    private lateinit var passOwnerIndexNames: List<String>

    @Execution
    fun createIndexes() {
        val passOwnerIdAndPurchasedAtIndexDefinition = CompoundIndexDefinition(
            Document()
                .append(MongoPass::passOwnerId.name, 1)
                .append(MongoPass::purchasedAt.name, 1)
        )
        mongoTemplate.indexOps<MongoPass>().ensureIndex(passOwnerIdAndPurchasedAtIndexDefinition).also { indexName ->
            passOwnerIdAndPurchasedAtIndexName = indexName
        }
        passOwnerIndexNames = mongoTemplate.indexOps<MongoPassOwner>().run {
            listOf(
                ensureIndex(Index(MongoPassOwner::email.name, ASC).unique()),
                ensureIndex(Index(MongoPassOwner::phoneNumber.name, ASC).unique())
            )
        }
    }

    @RollbackExecution
    fun dropIndexesInCaseOfFailure() {
        log.info("Rolling back ${this::class.simpleName}")

        val allPassIndexNames = indexNames<MongoPass>()
        if (allPassIndexNames.contains(passOwnerIdAndPurchasedAtIndexName)) {
            mongoTemplate.indexOps<MongoPass>().dropIndex(passOwnerIdAndPurchasedAtIndexName)
        }

        val passOwnerIndexOperations = mongoTemplate.indexOps<MongoPassOwner>()
        passOwnerIndexNames.filter { it in indexNames<MongoPassOwner>() }
            .forEach { passOwnerIndexOperations.dropIndex(it) }
    }

    private inline fun <reified T> indexNames(): List<String> {
        return mongoTemplate.indexOps(T::class.java).indexInfo.map { it.name }
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(InitialIndexCreationChangeUnit::class.java)
    }
}
