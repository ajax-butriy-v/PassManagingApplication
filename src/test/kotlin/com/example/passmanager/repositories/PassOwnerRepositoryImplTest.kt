package com.example.passmanager.repositories

import com.example.passmanager.domain.MongoPassOwner
import com.example.passmanager.testcontainers.WithMongoTestContainer
import com.example.passmanager.util.PassOwnerFixture.getOwnerWithUniqueFields
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
class PassOwnerRepositoryImplTest {
    @Autowired
    private lateinit var mongoTemplate: ReactiveMongoTemplate

    @Autowired
    private lateinit var passOwnerRepository: PassOwnerRepository

    @Test
    fun `finding pass by existing id should return pass owner by id`() {
        // GIVEN
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields())

        // WHEN
        val passOwnerById = insertedPassOwner
            .mapNotNull { it.id.toString() }
            .flatMap { insertedPassOwnerId -> passOwnerRepository.findById(insertedPassOwnerId) }

        // THEN
        passOwnerById.test()
            .assertNext { assertNotNull(it) }
            .verifyComplete()
    }

    @Test
    fun `inserting pass owner in collection should return created pass owner`() {
        // WHEN
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields())

        // THEN
        insertedPassOwner
            .mapNotNull { it.id }
            .test()
            .assertNext { ownerId ->
                mongoTemplate.findById<MongoPassOwner>(ownerId!!)
                    .mapNotNull { it.id }
                    .doOnNext { id -> assertThat(ownerId).isEqualTo(id) }
                    .subscribe()
            }
            .verifyComplete()
    }

    @Test
    fun `saving pass owner in collection show update existing pass owner by id`() {
        // GIVEN
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields())
        val changedFirstName = "Changed first name"
        val updatedPassOwner = insertedPassOwner.map { it.copy(firstName = changedFirstName) }

        // WHEN
        val saved = updatedPassOwner.flatMap { passOwnerRepository.save(it) }

        // WHEN
        saved.test()
            .assertNext { assertThat(it.firstName).isEqualTo(changedFirstName) }
            .verifyComplete()
    }

    @Test
    fun `deleting pass owner by id should delete pass owner from collection`() {
        // GIVEN
        // Cache the result to avoid re-triggering insert
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields()).cache()

        // WHEN
        insertedPassOwner
            .mapNotNull { it.id.toString() }
            .flatMap { ownerId -> passOwnerRepository.deleteById(ownerId) }
            .subscribe()

        // THEN
        insertedPassOwner
            .mapNotNull { it.id }
            .flatMap { mongoTemplate.exists<MongoPassOwner>(query(where("_id").isEqualTo(it))) }
            .test()
            .assertNext { exists -> assertFalse("Pass owner must not exist in collection after deletion") { exists } }
            .verifyComplete()
    }
}
