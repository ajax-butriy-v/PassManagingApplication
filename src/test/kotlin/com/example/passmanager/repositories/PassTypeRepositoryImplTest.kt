package com.example.passmanager.repositories

import com.example.passmanager.domain.MongoPassType
import com.example.passmanager.testcontainers.WithMongoTestContainer
import com.example.passmanager.util.OptimisticLockTestUtils.getOptimisticLocksAmount
import com.example.passmanager.util.PassFixture.passTypeToCreate
import com.example.passmanager.util.PassFixture.singlePassType
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.exists
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.isEqualTo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@SpringBootTest
@WithMongoTestContainer
class PassTypeRepositoryImplTest {
    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Autowired
    private lateinit var passTypeRepository: PassTypeRepository

    @Test
    fun `finding pass type by existing id should return pass type by id`() {
        // GIVEN
        val insertedPassType = mongoTemplate.insert(passTypeToCreate)
        val insertedPassTypeId = insertedPassType.id

        // WHEN
        val passTypeById = passTypeRepository.findById(insertedPassTypeId.toString())

        // THEN
        assertThat(passTypeById?.id).isNotNull().isEqualTo(insertedPassTypeId)
    }

    @Test
    fun `inserting pass type in collection should return created pass type`() {
        // WHEN
        val insertedPassType = mongoTemplate.insert(passTypeToCreate)
        val insertedPassTypeId = insertedPassType.id

        // THEN
        assertTrue("pass type must be persisted in db after creation") {
            mongoTemplate.exists<MongoPassType>(query(where("id").isEqualTo(insertedPassTypeId)))
        }
    }

    @Test
    fun `saving pass type in collection show update existing pass type by id`() {
        // GIVEN
        val insertedPassType = mongoTemplate.insert(passTypeToCreate)
        val changedTypeName = "Changed name"
        val updatedPassType = insertedPassType.copy(name = changedTypeName)

        // WHEN
        val saved = passTypeRepository.save(updatedPassType)

        // WHEN
        assertThat(saved.name).isEqualTo(changedTypeName)
    }

    @Test
    fun `deleting pass type by id should delete pass type from collection`() {
        // GIVEN
        val insertedPassType = mongoTemplate.insert(passTypeToCreate)
        val insertedPassTypeId = insertedPassType.id

        // WHEN
        passTypeRepository.deleteById(insertedPassTypeId.toString())

        // WHEN
        assertFalse("pass owner must not exist in collection after deletion") {
            mongoTemplate.exists<MongoPassType>(query(where("_id").isEqualTo(insertedPassTypeId)))
        }
    }

    @Test
    fun `optimistic lock handling while save() should throw exception if version was changed by another thread`() {
        // GIVEN
        val insertedPassType = passTypeRepository.insert(singlePassType)
        val typeNames = listOf("First", "Second", "Third")
        assertEquals(1, insertedPassType.version)

        // WHEN
        val tasks = typeNames.map {
            Runnable { passTypeRepository.save(insertedPassType.copy(name = it)) }
        }
        val optimisticLocks = getOptimisticLocksAmount(tasks)

        // THEN
        val updatedPass = passTypeRepository.findById(insertedPassType.id.toString())
        assertEquals(2, updatedPass?.version)
        assertEquals(2, optimisticLocks)
    }
}
