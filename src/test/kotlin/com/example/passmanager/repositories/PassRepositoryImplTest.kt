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
    fun `finding pass by existing id should return pass by id`() {
        // GIVEN
        val inserted = mongoTemplate.insert(passToCreate)

        // WHEN
        val passById = passRepository.findById(inserted.id.toString())

        // THEN
        assertThat(passById?.id).isNotNull().isEqualTo(inserted.id)
    }

    @Test
    fun `inserting pass in collection should return created pass`() {
        // WHEN
        val inserted = passRepository.insert(passToCreate)
        val insertedId = inserted.id

        // THEN
        assertTrue("pass must be persisted in db after creation") {
            mongoTemplate.exists<MongoPass>(query(where("id").isEqualTo(insertedId)))
        }
    }

    @Test
    fun `saving pass in collection show update existing pass by id`() {
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
    fun `deleting pass by id should delete pass from collection`() {
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
    fun `deleting all passes by owner id should delete all owner's passes`() {
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
    fun `finding by owner and purchased after should return corresponding passes`() {
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
    fun `getting passes price distributions should return correct distribution per type`() {
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
    fun `getting sum of purchased passes for pass owner should return correct sum`() {
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
    fun `getting sum of purchased passes for pass owner with no passes should return zero sum`() {
        // GIVEN
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields())
        val insertedPassOwnerId = insertedPassOwner.id
        val afterDate = LocalDate.now()

        // WHEN
        val sum = passRepository.sumPurchasedAtAfterDate(insertedPassOwnerId.toString(), afterDate)

        // THEN
        assertThat(sum).isEqualTo(BigDecimal.ZERO)
    }

    @Test
    fun `optimistic lock handling while save() should throw exception if version was changed by another thread`() {
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
