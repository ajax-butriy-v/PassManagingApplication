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
    fun `test finding pass owner by id`() {
        // GIVEN
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields())
        val insertedPassOwnerId = insertedPassOwner.id

        // WHEN
        val passOwnerById = passOwnerRepository.findById(insertedPassOwnerId.toString())

        // THEN
        assertThat(passOwnerById?.id).isNotNull().isEqualTo(insertedPassOwnerId)
    }

    @Test
    fun `test inserting pass owner into collection`() {
        // WHEN
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields())
        val insertedPassOwnerId = insertedPassOwner.id

        // THEN
        assertThat(mongoTemplate.findById<MongoPassOwner>(insertedPassOwnerId!!)).isEqualTo(insertedPassOwner)
    }

    @Test
    fun `test saving pass owner into collection`() {
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
    fun `test deleting pass from collection`() {
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
    fun `testing optimistic lock handling while save()`() {
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
