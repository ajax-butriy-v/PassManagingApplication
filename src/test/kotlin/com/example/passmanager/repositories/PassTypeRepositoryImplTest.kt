package com.example.passmanager.repositories

import com.example.passmanager.domain.MongoPassType
import com.example.passmanager.testcontainers.WithMongoTestContainer
import com.example.passmanager.util.PassFixture.passTypeToCreate
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.exists
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.isEqualTo
import reactor.kotlin.test.test
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

@SpringBootTest
@WithMongoTestContainer
class PassTypeRepositoryImplTest {
    @Autowired
    private lateinit var mongoTemplate: ReactiveMongoTemplate

    @Autowired
    private lateinit var passTypeRepository: PassTypeRepository

    @Test
    fun `finding pass type by existing id should return pass type by id`() {
        // GIVEN
        val insertedPassType = mongoTemplate.insert(passTypeToCreate)

        // WHEN
        val passTypeById = insertedPassType
            .mapNotNull { it.id.toString() }
            .flatMap { passTypeId -> passTypeRepository.findById(passTypeId) }

        // THEN
        passTypeById.test()
            .assertNext { assertNotNull(it) }
            .verifyComplete()
    }

    @Test
    fun `inserting pass type in collection should return created pass type`() {
        // WHEN
        val insertedPassType = mongoTemplate.insert(passTypeToCreate)

        // THEN
        insertedPassType
            .mapNotNull { it.id }
            .test()
            .assertNext { passTypeId ->
                mongoTemplate.findById<MongoPassType>(passTypeId!!)
                    .mapNotNull { it.id }
                    .doOnNext { id -> assertThat(passTypeId).isEqualTo(id) }
                    .subscribe()
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
        // Cache the result to avoid re-triggering insert
        val insertedPassType = mongoTemplate.insert(passTypeToCreate).cache()

        // WHEN
        insertedPassType
            .mapNotNull { it.id.toString() }
            .flatMap { passTypeId -> passTypeRepository.deleteById(passTypeId) }
            .subscribe()

        // THEN
        insertedPassType
            .mapNotNull { it.id }
            .flatMap { mongoTemplate.exists<MongoPassType>(query(where("_id").isEqualTo(it))) }
            .test()
            .assertNext { exists -> assertFalse("pass type must not exist in collection after deletion") { exists } }
            .verifyComplete()
    }
}
