package com.example.passmanager.repositories

import com.example.passmanager.domain.MongoPass
import com.example.passmanager.domain.MongoPassOwner
import com.example.passmanager.domain.MongoPassType
import com.example.passmanager.testcontainers.WithMongoTestContainer
import com.example.passmanager.util.OptimisticLockTestUtils.getOptimisticLocksAmount
import com.example.passmanager.util.PassFixture.passToCreate
import com.example.passmanager.util.PassFixture.passTypes
import com.example.passmanager.util.PassFixture.passes
import com.example.passmanager.util.PassFixture.singlePassId
import com.example.passmanager.util.PassOwnerFixture.passOwnerFromDb
import com.example.passmanager.util.PassOwnerFixture.passOwnerIdFromDb
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.count
import org.springframework.data.mongodb.core.exists
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.remove
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@SpringBootTest
@WithMongoTestContainer
class PassRepositoryImplTest {

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Autowired
    private lateinit var passRepository: PassRepository

    @AfterTest
    fun clearData() {
        mongoTemplate.remove<MongoPass>(Query())
        mongoTemplate.remove<MongoPassOwner>(Query())
        mongoTemplate.remove<MongoPassType>()
    }

    @Test
    fun `test finding pass by id`() {
        mongoTemplate.insertAll(passes)

        // GIVEN
        val passById = passRepository.findById(singlePassId)

        // WHEN
        assertThat(passById?.id.toString()).isNotNull().isEqualTo(singlePassId)
    }

    @Test
    fun `test inserting pass in collection`() {
        // GIVEN
        val inserted = passRepository.insert(passToCreate)

        // WHEN
        assertThat(inserted.id).isNotNull()
        assertThat(mongoTemplate.count<MongoPass>()).isEqualTo(1)
    }

    @Test
    fun `test saving pass in collection`() {
        val inserted = mongoTemplate.insert(passToCreate)

        // GIVEN
        val changedPrice = BigDecimal.TEN
        val updatedPass = inserted.copy(purchasedFor = changedPrice)
        val saved = passRepository.save(updatedPass)

        // WHEN
        assertThat(saved.purchasedFor).isEqualTo(changedPrice)
    }

    @Test
    fun `test deleting pass by id`() {
        mongoTemplate.insertAll(passes)

        // GIVEN
        passRepository.deleteById(singlePassId)

        // WHEN
        assertFalse(
            message = "pass must not exist in db after deletion",
            { mongoTemplate.exists<MongoPass>(query(where("id").`is`(singlePassId))) }
        )
    }

    @Test
    fun `test deleting all passes by owner id`() {
        mongoTemplate.insertAll(passes)

        // GIVEN
        passRepository.deleteAllByOwnerId(passOwnerIdFromDb)

        // WHEN
        assertFalse(
            message = "all owner passes must not exist in db after deletion",
            { mongoTemplate.exists<MongoPass>(query(where("passOwner._id").`is`(ObjectId(passOwnerIdFromDb)))) }
        )
    }

    @Test
    fun `test deleting pass by id and owner id`() {
        mongoTemplate.insertAll(passes)

        // GIVEN
        passRepository.deleteByIdAndOwnerId(singlePassId, passOwnerIdFromDb)

        // WHEN
        assertFalse(
            message = "pass must not exist in db after deletion",
            {
                mongoTemplate.exists<MongoPass>(
                    query(
                        where("passOwner._id").`is`(ObjectId(passOwnerIdFromDb))
                            .andOperator(where("_id").`is`(singlePassId))
                    )
                )
            }
        )
    }

    @Test
    fun `test finding by owner and purchased after`() {
        mongoTemplate.insertAll(passes)

        // GIVEN
        val afterDate = LocalDate.now()
        val passes = passRepository.findByOwnerAndPurchasedAfter(passOwnerIdFromDb, LocalDate.now())

        // WHEN
        assertThat(passes).hasSize(3)
            .allMatch({ it.passOwnerId.toString() == passOwnerIdFromDb })
            .allMatch({
                val dateToInstant = afterDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
                it.purchasedAt?.isAfter(dateToInstant) == true
            })
    }

    @Test
    fun `finding all by pass owner id should return corresponding passes`() {
        mongoTemplate.insert(passOwnerFromDb)
        mongoTemplate.insertAll(passes)

        // GIVEN
        val passesByOwnerId = passRepository.findAllByPassOwnerId(passOwnerIdFromDb)

        // WHEN
        assertThat(passesByOwnerId).hasSize(3)
            .allMatch({ it.passOwnerId.toString() == passOwnerIdFromDb })
    }

    @Test
    fun `finding all by non-existent pass owner id should return empty list`() {
        mongoTemplate.insert(passOwnerFromDb)
        mongoTemplate.insertAll(passes)

        // GIVEN
        val passesByOwnerId = passRepository.findAllByPassOwnerId(ObjectId.get().toString())

        // WHEN
        assertThat(passesByOwnerId).isEmpty()
    }

    @Test
    fun `test getting passes price distributions`() {
        mongoTemplate.insertAll(passes)
        mongoTemplate.insertAll(passTypes)

        // GIVEN
        val priceDistributions = passRepository.getPassesPriceDistribution(passOwnerIdFromDb)
        // WHEN
        assertThat(priceDistributions).hasSize(3)
            .allMatch({ it.spentForPassType == BigDecimal.TEN })
    }

    @Test
    fun `test getting sum of purchased passes for pass owner`() {
        mongoTemplate.insertAll(passes)

        // GIVEN
        val afterDate = LocalDate.now()
        val sum = passRepository.sumPurchasedAtAfterDate(passOwnerIdFromDb, afterDate)

        // WHEN
        assertThat(sum).isEqualTo(BigDecimal.valueOf(30))
    }

    @Test
    fun `getting sum of purchased passes for non-existent pass owner should return zero sum`() {
        mongoTemplate.insertAll(passes)

        // GIVEN
        val afterDate = LocalDate.now()
        val sum = passRepository.sumPurchasedAtAfterDate(ObjectId.get().toString(), afterDate)

        // WHEN
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
