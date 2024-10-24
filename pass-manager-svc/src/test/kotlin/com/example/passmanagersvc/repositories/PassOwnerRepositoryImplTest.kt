package com.example.passmanagersvc.repositories

import com.example.passmanagersvc.domain.MongoPassOwner
import com.example.passmanagersvc.util.IntegrationTest
import com.example.passmanagersvc.util.PassOwnerFixture.getOwnerWithUniqueFields
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

internal class PassOwnerRepositoryImplTest : IntegrationTest() {
    @Autowired
    private lateinit var mongoTemplate: ReactiveMongoTemplate

    @Autowired
    private lateinit var passOwnerRepository: PassOwnerRepository

    @Test
    fun `finding pass by existing id should return pass owner by id`() {
        // GIVEN
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields()).block()

        // WHEN
        val passOwnerById = passOwnerRepository.findById(insertedPassOwner!!.id.toString())

        // THEN
        passOwnerById.test()
            .assertNext { assertThat(it).isEqualTo(insertedPassOwner) }
            .verifyComplete()
    }

    @Test
    fun `inserting pass owner in collection should return created pass owner`() {
        // GIVEN
        val ownerToCreate = getOwnerWithUniqueFields()
        val insertedPassOwner = mongoTemplate.insert(ownerToCreate).block()

        // WHEN
        val passOwnerById = passOwnerRepository.findById(insertedPassOwner!!.id.toString())

        // THEN
        assertThat(insertedPassOwner).isEqualTo(
            ownerToCreate.copy(
                id = insertedPassOwner.id,
                version = insertedPassOwner.version
            )
        )

        passOwnerById.test()
            .assertNext { assertThat(it).isEqualTo(insertedPassOwner) }
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
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields()).block()
        val insertedPassOwnerId = insertedPassOwner!!.id

        // WHEN
        val delete = passOwnerRepository.deleteById(insertedPassOwnerId.toString())

        // THEN
        delete.test()
            .expectNext(Unit)
            .verifyComplete()

        assertFalse("Pass owner must not exist in collection after deletion") {
            val existsById = mongoTemplate.exists<MongoPassOwner>(
                query(where(Fields.UNDERSCORE_ID).isEqualTo(insertedPassOwnerId))
            )
            existsById.block() == true
        }
    }
}
