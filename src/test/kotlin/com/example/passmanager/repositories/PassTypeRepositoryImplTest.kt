package com.example.passmanager.repositories

import com.example.passmanager.domain.MongoPassType
import com.example.passmanager.testcontainers.WithMongoTestContainer
import com.example.passmanager.util.OptimisticLockTestUtils.getOptimisticLocksAmount
import com.example.passmanager.util.PassFixture.singlePassType
import com.example.passmanager.util.PassFixture.singlePassTypeId
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.count
import org.springframework.data.mongodb.core.exists
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.remove
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@SpringBootTest
@WithMongoTestContainer
class PassTypeRepositoryImplTest {
    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Autowired
    private lateinit var passTypeRepository: PassTypeRepository

    @AfterTest
    fun clearData() {
        mongoTemplate.remove<MongoPassType>(Query())
    }

    @Test
    fun `test finding pass type by id`() {
        mongoTemplate.insert(singlePassType)

        // GIVEN
        val passTypeById = passTypeRepository.findById(singlePassTypeId)

        // WHEN
        assertThat(passTypeById?.id.toString()).isNotNull().isEqualTo(singlePassTypeId)
    }

    @Test
    fun `test inserting pass type into collection`() {
        // GIVEN
        val inserted = passTypeRepository.insert(singlePassType)

        // WHEN
        assertThat(inserted.id).isNotNull()
        assertThat(mongoTemplate.count<MongoPassType>()).isEqualTo(1)
    }

    @Test
    fun `test saving pass type into collection`() {
        val inserted = mongoTemplate.insert(singlePassType.copy(id = null))

        // GIVEN
        val changedTypeName = "Changed name"
        val updatedPassType = inserted.copy(name = changedTypeName)
        val saved = passTypeRepository.save(updatedPassType)

        // WHEN
        assertThat(saved.name).isEqualTo(changedTypeName)
    }

    @Test
    fun `test deleting pass type from collection`() {
        mongoTemplate.insert(singlePassType)

        // GIVEN
        passTypeRepository.deleteById(singlePassTypeId)

        // WHEN
        assertFalse(
            message = "pass owner must not exist in collection after deletion",
            { mongoTemplate.exists<MongoPassType>(query(where("id").`is`(singlePassType))) }
        )
    }

    @Test
    fun `testing optimistic lock handling while save()`() {
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
