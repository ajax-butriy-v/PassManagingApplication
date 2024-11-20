package com.example.passmanagersvc.passowner.infrastructure.mongo.repository

import com.example.passmanagersvc.passowner.infrastructure.mongo.entity.MongoPassOwner
import com.example.passmanagersvc.passowner.infrastructure.mongo.mapper.PassOwnerMapper.toDomain
import com.example.passmanagersvc.passowner.util.IntegrationTest
import com.example.passmanagersvc.util.PassFixture.mongoPassTypesToCreate
import com.example.passmanagersvc.util.PassFixture.passTypes
import com.example.passmanagersvc.util.PassFixture.passesToCreate
import com.example.passmanagersvc.util.PassOwnerFixture.getOwnerWithUniqueFields
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Fields
import org.springframework.data.mongodb.core.exists
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.isEqualTo
import reactor.kotlin.test.test
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertFalse

internal class MongoPassOwnerRepositoryTest : IntegrationTest() {
    @Autowired
    private lateinit var mongoTemplate: ReactiveMongoTemplate

    @Autowired
    private lateinit var passOwnerRepository: MongoPassOwnerRepository

    @Test
    fun `finding pass by existing id should return pass owner by id`() {
        // GIVEN
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields()).block()

        // WHEN
        val passOwnerById = passOwnerRepository.findById(insertedPassOwner!!.id.toString())

        // THEN
        passOwnerById.test()
            .assertNext { assertThat(it).isEqualTo(insertedPassOwner.toDomain()) }
            .verifyComplete()
    }

    @Test
    fun `inserting pass owner in collection should return created pass owner`() {
        // GIVEN
        val ownerToCreate = getOwnerWithUniqueFields()
        val insertedPassOwner = mongoTemplate.insert(ownerToCreate).block()

        // WHEN
        val passOwnerById = passOwnerRepository.findById(insertedPassOwner!!.id.toString())

        // THEN
        assertThat(insertedPassOwner).isEqualTo(
            ownerToCreate.copy(
                id = insertedPassOwner.id,
                version = insertedPassOwner.version
            )
        )

        passOwnerById.test()
            .assertNext { assertThat(it).isEqualTo(insertedPassOwner.toDomain()) }
            .verifyComplete()
    }

    @Test
    fun `saving pass owner in collection show update existing pass owner by id`() {
        // GIVEN
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields())
        val changedFirstName = "Changed first name"
        val updatedPassOwner = insertedPassOwner.map { it.copy(firstName = changedFirstName) }

        // WHEN
        val saved = updatedPassOwner.flatMap { passOwnerRepository.save(it.toDomain()) }

        // WHEN
        saved.test()
            .assertNext { assertThat(it.firstName).isEqualTo(changedFirstName) }
            .verifyComplete()
    }

    @Test
    fun `deleting pass owner by id should delete pass owner from collection`() {
        // GIVEN
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields()).block()
        val insertedPassOwnerId = insertedPassOwner!!.id

        // WHEN
        val delete = passOwnerRepository.deleteById(insertedPassOwnerId.toString())

        // THEN
        delete.test()
            .expectNext(Unit)
            .verifyComplete()

        assertFalse("Pass owner must not exist in collection after deletion") {
            val existsById = mongoTemplate.exists<MongoPassOwner>(
                query(where(Fields.UNDERSCORE_ID).isEqualTo(insertedPassOwnerId))
            )
            existsById.block() == true
        }
    }

    @Test
    fun `getting passes price distributions should return correct distribution per type`() {
        // GIVEN
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields()).block()!!
        val passTypes = mongoTemplate.insertAll(mongoPassTypesToCreate).collectList().block()!!
        mongoTemplate.insertAll(passesToCreate(insertedPassOwner.id!!, passTypes)).collectList().block()!!

        // WHEN
        val priceDistributionFlux = passOwnerRepository.getPassesPriceDistribution(insertedPassOwner.id.toString())

        // THEN
        priceDistributionFlux.collectList()
            .test()
            .assertNext { priceDistributions ->
                assertThat(priceDistributions).hasSize(3).allMatch { it.spentForPassType == BigDecimal.TEN }
            }
            .verifyComplete()
    }

    @Test
    fun `getting sum of purchased passes for pass owner should return correct sum`() {
        // GIVEN
        val passOwner = mongoTemplate.insert(getOwnerWithUniqueFields()).block()!!
        val passOwnerId = passOwner.id!!
        mongoTemplate.insertAll(passTypes).collectList().block()!!
        mongoTemplate.insertAll(passesToCreate(passOwner.id!!)).collectList().block()!!
        val afterDate = LocalDate.now()

        // WHEN
        val sum = passOwnerRepository.sumPurchasedAtAfterDate(passOwnerId.toString(), afterDate)

        // THEN
        sum.test()
            .assertNext { assertThat(it).isEqualTo(BigDecimal.valueOf(30)) }
            .verifyComplete()
    }

    @Test
    fun `getting sum of purchased passes for pass owner with no passes should return zero sum`() {
        // GIVEN
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields())
        val afterDate = LocalDate.now()

        // WHEN
        val sum = insertedPassOwner
            .mapNotNull { it.id.toString() }
            .flatMap { passOwnerRepository.sumPurchasedAtAfterDate(it, afterDate) }

        // THEN
        sum.test()
            .assertNext { assertThat(it).isEqualTo(BigDecimal.ZERO) }
            .verifyComplete()
    }
}
