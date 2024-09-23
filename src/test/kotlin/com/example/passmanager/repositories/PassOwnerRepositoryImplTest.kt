package com.example.passmanager.repositories

import com.example.passmanager.domain.MongoPassOwner
import com.example.passmanager.testcontainers.WithMongoTestContainer
import com.example.passmanager.util.OptimisticLockTestUtils.getOptimisticLocksAmount
import com.example.passmanager.util.PassOwnerFixture.getOwnerWithUniqueFields
import com.example.passmanager.util.PassOwnerFixture.passOwnerToCreate
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.exists
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.isEqualTo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@SpringBootTest
@WithMongoTestContainer
class PassOwnerRepositoryImplTest {
    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Autowired
    private lateinit var passOwnerRepository: PassOwnerRepository

    @Test
    fun `finding pass by existing id should return pass owner by id`() {
        // GIVEN
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields())
        val insertedPassOwnerId = insertedPassOwner.id

        // WHEN
        val passOwnerById = passOwnerRepository.findById(insertedPassOwnerId.toString())

        // THEN
        assertThat(passOwnerById?.id).isNotNull().isEqualTo(insertedPassOwnerId)
    }

    @Test
    fun `inserting pass owner in collection should return created pass owner`() {
        // WHEN
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields())
        val insertedPassOwnerId = insertedPassOwner.id

        // THEN
        assertThat(mongoTemplate.findById<MongoPassOwner>(insertedPassOwnerId!!)).isEqualTo(insertedPassOwner)
    }

    @Test
    fun `saving pass owner in collection show update existing pass owner by id`() {
        // GIVEN
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields())
        val changedFirstName = "Changed first name"
        val updatedPassOwner = insertedPassOwner.copy(firstName = changedFirstName)

        // WHEN
        val saved = passOwnerRepository.save(updatedPassOwner)

        // WHEN
        assertThat(saved.firstName).isEqualTo(changedFirstName)
    }

    @Test
    fun `deleting pass owner by id should delete pass owner from collection`() {
        // GIVEN
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields())
        val insertedPassOwnerId = insertedPassOwner.id

        // WHEN
        passOwnerRepository.deleteById(insertedPassOwnerId.toString())

        // THEN
        assertFalse("pass owner must not exist in collection after deletion") {
            mongoTemplate.exists<MongoPassOwner>(query(where("_id").isEqualTo(insertedPassOwnerId)))
        }
    }

    @Test
    fun `optimistic lock handling while save() should throw exception if version was changed by another thread()`() {
        // GIVEN
        val insertedPassOwner = passOwnerRepository.insert(passOwnerToCreate)
        val firstNames = listOf("Firstname1", "Firstname2")
        assertEquals(1, insertedPassOwner.version)

        // WHEN
        val tasks = firstNames.map {
            Runnable { passOwnerRepository.save(insertedPassOwner.copy(firstName = it)) }
        }
        val optimisticLocks = getOptimisticLocksAmount(tasks)

        // THEN
        val updatedPass = passOwnerRepository.findById(insertedPassOwner.id.toString())
        assertEquals(2, updatedPass?.version)
        assertEquals(1, optimisticLocks)
    }
}
