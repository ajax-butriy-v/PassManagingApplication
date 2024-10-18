package com.example.passmanagersvc.repositories

import com.example.passmanagersvc.domain.MongoPassType
import com.example.passmanagersvc.util.IntegrationTest
import com.example.passmanagersvc.util.PassFixture.passTypeToCreate
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Fields
import org.springframework.data.mongodb.core.exists
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.isEqualTo
import reactor.kotlin.test.test
import kotlin.test.Test
import kotlin.test.assertFalse

internal class PassTypeRepositoryImplTest : IntegrationTest() {
    @Autowired
    private lateinit var mongoTemplate: ReactiveMongoTemplate

    @Autowired
    private lateinit var passTypeRepository: PassTypeRepository

    @Test
    fun `finding pass type by existing id should return pass type by id`() {
        // GIVEN
        val insertedPassType = mongoTemplate.insert(passTypeToCreate).block()

        // WHEN
        val passTypeById = passTypeRepository.findById(insertedPassType!!.id.toString())

        // THEN
        passTypeById.test()
            .assertNext {
                assertThat(it).isEqualTo(insertedPassType)
            }
            .verifyComplete()
    }

    @Test
    fun `inserting pass type in collection should return created pass type`() {
        // GIVEN
        val insertedPassType = mongoTemplate.insert(passTypeToCreate).block()

        // WHEN
        val passTypeById = passTypeRepository.findById(insertedPassType!!.id.toString())

        // THEN
        assertThat(insertedPassType).isEqualTo(
            passTypeToCreate.copy(
                id = insertedPassType.id,
                version = insertedPassType.version
            )
        )

        passTypeById.test()
            .assertNext {
                assertThat(it).isEqualTo(insertedPassType)
            }
            .verifyComplete()
    }

    @Test
    fun `saving pass type in collection should update existing pass type by id`() {
        // GIVEN
        val insertedPassType = mongoTemplate.insert(passTypeToCreate)
        val changedTypeName = "Changed name"
        val updatedPassType = insertedPassType.map { it.copy(name = changedTypeName) }

        // WHEN
        val savedPassType = updatedPassType.flatMap { passTypeRepository.save(it) }

        // THEN
        savedPassType.test()
            .assertNext { saved -> assertThat(saved.name).isEqualTo(changedTypeName) }
            .verifyComplete()
    }

    @Test
    fun `deleting pass type by id should delete pass type from collection`() {
        // GIVEN
        val insertedPassType = mongoTemplate.insert(passTypeToCreate).block()
        val insertedPassTypeId = insertedPassType!!.id

        // WHEN
        val delete = passTypeRepository.deleteById(insertedPassTypeId.toString())

        // THEN
        delete.test()
            .expectNext(Unit)
            .verifyComplete()

        assertFalse("pass type must not exist in collection after deletion") {
            val existsById = mongoTemplate.exists<MongoPassType>(
                query(where(Fields.UNDERSCORE_ID).isEqualTo(insertedPassTypeId))
            )
            existsById.block() == true
        }
    }
}
