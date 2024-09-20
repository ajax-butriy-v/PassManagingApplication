package com.example.passmanager.repositories

import com.example.passmanager.domain.MongoPass
import com.example.passmanager.testcontainers.WithMongoTestContainer
import com.example.passmanager.util.OptimisticLockTestUtils.getOptimisticLocksAmount
import com.example.passmanager.util.PassFixture.passToCreate
import com.example.passmanager.util.PassFixture.passTypesToCreate
import com.example.passmanager.util.PassFixture.passesToCreate
import com.example.passmanager.util.PassOwnerFixture.getOwnerWithUniqueFields
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.exists
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.isEqualTo
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@SpringBootTest
@WithMongoTestContainer
class PassRepositoryImplTest {

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Autowired
    private lateinit var passRepository: PassRepository

    @Test
    fun `test finding pass by id`() {
        // GIVEN
        val inserted = mongoTemplate.insert(passToCreate)

        // WHEN
        val passById = passRepository.findById(inserted.id.toString())

        // THEN
        assertThat(passById?.id).isNotNull().isEqualTo(inserted.id)
    }

    @Test
    fun `test inserting pass in collection`() {
        // WHEN
        val inserted = passRepository.insert(passToCreate)
        val insertedId = inserted.id

        // THEN
        assertTrue("pass must be persisted in db after creation") {
            mongoTemplate.exists<MongoPass>(query(where("id").isEqualTo(insertedId)))
        }
    }

    @Test
    fun `test saving pass in collection`() {
        // GIVEN
        val inserted = mongoTemplate.insert(passToCreate)
        val changedPrice = BigDecimal.TEN
        val updatedPass = inserted.copy(purchasedFor = changedPrice)

        // WHEN
        val saved = passRepository.save(updatedPass)

        // THEN
        assertThat(saved.purchasedFor).isEqualTo(changedPrice)
    }

    @Test
    fun `test deleting pass by id`() {
        // GIVEN
        val inserted = mongoTemplate.insert(passToCreate)
        val insertedId = inserted.id

        // WHEN
        passRepository.deleteById(insertedId.toString())

        // THEN
        assertFalse("pass must not exist in db after deletion") {
            mongoTemplate.exists<MongoPass>(query(where("id").isEqualTo(insertedId)))
        }
    }

    @Test
    fun `test deleting all passes by owner id`() {
        // GIVEN
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields())
        val insertedPassOwnerId = insertedPassOwner.id
        mongoTemplate.insertAll(passesToCreate.map { it.copy(passOwnerId = insertedPassOwnerId) })

        // WHEN
        passRepository.deleteAllByOwnerId(insertedPassOwnerId.toString())

        // THEN
        assertFalse(message = "all owner passes must not exist in db after deletion") {
            mongoTemplate.exists<MongoPass>(query(where("passOwnerId").isEqualTo(insertedPassOwnerId)))
        }
    }

    @Test
    fun `test deleting pass by id and owner id`() {
        // GIVEN
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields())
        val insertedPassOwnerId = insertedPassOwner.id
        val insertedPass = mongoTemplate.insert(passToCreate.copy(passOwnerId = insertedPassOwnerId))
        val insertedPassId = insertedPass.id

        // WHEN
        passRepository.deleteByIdAndOwnerId(insertedPassId.toString(), insertedPassOwnerId.toString())

        // THEN
        assertFalse("pass must not exist in db after deletion") {
            mongoTemplate.exists<MongoPass>(
                query(
                    where("passOwnerId").isEqualTo(insertedPassOwnerId)
                        .andOperator(where("_id").isEqualTo(insertedPassId))
                )
            )
        }
    }

    @Test
    fun `test finding by owner and purchased after`() {
        // GIVEN
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields())
        val insertedPassOwnerId = insertedPassOwner.id
        mongoTemplate.insertAll(passesToCreate.map { it.copy(passOwnerId = insertedPassOwnerId) })
        val afterDate = LocalDate.now()

        // WHEN
        val passes = passRepository.findByOwnerAndPurchasedAfter(insertedPassOwnerId.toString(), afterDate)

        // THEN
        assertThat(passes).hasSize(3)
            .allMatch({ it.passOwnerId == insertedPassOwnerId })
            .allMatch({
                val dateToInstant = afterDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
                it.purchasedAt?.isAfter(dateToInstant) == true
            })
    }

    @Test
    fun `finding all by pass owner id should return corresponding passes`() {
        // GIVEN
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields())
        val insertedPassOwnerId = insertedPassOwner.id
        mongoTemplate.insertAll(passesToCreate.map { it.copy(passOwnerId = insertedPassOwnerId) })

        // WHEN
        val passesByOwnerId = passRepository.findAllByPassOwnerId(insertedPassOwnerId.toString())

        // THEN
        assertThat(passesByOwnerId).hasSize(3).allMatch({ it.passOwnerId == insertedPassOwnerId })
    }

    @Test
    fun `finding all by non-existent pass owner id should return empty list`() {
        // GIVEN
        mongoTemplate.insertAll(passesToCreate)

        // WHEN
        val passesByOwnerId = passRepository.findAllByPassOwnerId(ObjectId.get().toString())

        // THEN
        assertThat(passesByOwnerId).isEmpty()
    }

    @Test
    fun `test getting passes price distributions`() {
        // GIVEN
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields())
        val insertedPassOwnerId = insertedPassOwner.id
        val insertedPassTypes = mongoTemplate.insertAll(passTypesToCreate)
        val passesToCreate = insertedPassTypes.map {
            passToCreate.copy(passOwnerId = insertedPassOwnerId, passTypeId = it.id)
        }
        mongoTemplate.insertAll(passesToCreate)

        // WHEN
        val priceDistributions = passRepository.getPassesPriceDistribution(insertedPassOwnerId.toString())

        // THEN
        assertThat(priceDistributions).hasSize(3).allMatch({ it.spentForPassType == BigDecimal.TEN })
    }

    @Test
    fun `test getting sum of purchased passes for pass owner`() {
        // GIVEN
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields())
        val insertedPassOwnerId = insertedPassOwner.id
        mongoTemplate.insertAll(passesToCreate.map { it.copy(passOwnerId = insertedPassOwnerId) })
        val afterDate = LocalDate.now()

        // WHEN
        val sum = passRepository.sumPurchasedAtAfterDate(insertedPassOwnerId.toString(), afterDate)

        // THEN
        assertThat(sum).isEqualTo(BigDecimal.valueOf(30))
    }

    @Test
    fun `getting sum of purchased passes for non-existent pass owner should return zero sum`() {
        // GIVEN
        mongoTemplate.insertAll(passesToCreate)
        val afterDate = LocalDate.now()

        // WHEN
        val sum = passRepository.sumPurchasedAtAfterDate(ObjectId.get().toString(), afterDate)

        // THEN
        assertThat(sum).isEqualTo(BigDecimal.ZERO)
    }

    @Test
    fun `testing optimistic lock handling while save()`() {
        // GIVEN
        val createdPass = passRepository.insert(passToCreate)
        val priceChangingList = listOf(BigDecimal.valueOf(20), BigDecimal.valueOf(30))
        assertEquals(1, createdPass.version)

        // WHEN
        val tasks = priceChangingList.map {
            Runnable { passRepository.save(createdPass.copy(purchasedFor = it)) }
        }
        val optimisticLocks = getOptimisticLocksAmount(tasks)

        // THEN
        val updatedPass = passRepository.findById(createdPass.id.toString())
        assertEquals(2, updatedPass?.version)
        assertEquals(1, optimisticLocks)
    }
}
