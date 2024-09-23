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
import org.springframework.data.domain.Sort.Direction.DESC
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.indexOps

@ChangeUnit(id = "Create indexes", order = "1", author = "Vitalii Butriy")
internal class InitialIndexCreationChangeUnit(private val mongoTemplate: MongoTemplate) {
    private lateinit var passOwnerIdAndPurchasedAtIndex: String

    @Execution
    fun createIndexes() {
        val passOwnerIdAndPurchasedAtIndexDefinition = CompoundIndexDefinition(
            Document()
                .append(MongoPass::passOwnerId.name, 1)
                .append(MongoPass::purchasedAt.name, 1)
        )
        mongoTemplate.indexOps<MongoPass>().ensureIndex(passOwnerIdAndPurchasedAtIndexDefinition).also { indexName ->
            passOwnerIdAndPurchasedAtIndex = indexName
        }

        mongoTemplate.indexOps<MongoPassOwner>().ensureIndex(Index(MongoPassOwner::email.name, DESC).unique())
        mongoTemplate.indexOps<MongoPassOwner>().ensureIndex(Index(MongoPassOwner::phoneNumber.name, ASC).unique())
    }

    @RollbackExecution
    fun dropIndexesInCaseOfFailure() {
        log.info("Rolling back ${this::class.simpleName}")

        mongoTemplate.indexOps<MongoPass>().dropIndex(passOwnerIdAndPurchasedAtIndex)
        mongoTemplate.indexOps<MongoPassOwner>().dropIndex(MongoPassOwner::email.name)
        mongoTemplate.indexOps<MongoPassOwner>().dropIndex(MongoPassOwner::phoneNumber.name)
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(InitialIndexCreationChangeUnit::class.java)
    }
}
