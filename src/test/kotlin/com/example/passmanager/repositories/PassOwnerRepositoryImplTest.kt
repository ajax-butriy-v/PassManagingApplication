package com.example.passmanager.repositories

import com.example.passmanager.domain.MongoPassOwner
import com.example.passmanager.testcontainers.WithMongoTestContainer
import com.example.passmanager.util.PassOwnerFixture.passOwnerFromDb
import com.example.passmanager.util.PassOwnerFixture.passOwnerIdFromDb
import com.example.passmanager.util.PassOwnerFixture.passOwnerToCreate
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
import kotlin.test.assertFalse

@SpringBootTest
@WithMongoTestContainer
class PassOwnerRepositoryImplTest {
    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Autowired
    private lateinit var passOwnerRepository: PassOwnerRepository

    @AfterTest
    fun clearData() {
        mongoTemplate.remove<MongoPassOwner>(Query())
    }

    @Test
    fun `test finding pass owner by id`() {
        mongoTemplate.insert(passOwnerFromDb)

        // GIVEN
        val passOwnerById = passOwnerRepository.findById(passOwnerIdFromDb)

        // WHEN
        assertThat(passOwnerById?.id.toString()).isNotNull().isEqualTo(passOwnerIdFromDb)
    }

    @Test
    fun `test inserting pass owner into collection`() {
        // GIVEN
        val inserted = passOwnerRepository.insert(passOwnerToCreate)

        // WHEN
        assertThat(inserted.id).isNotNull()
        assertThat(mongoTemplate.count<MongoPassOwner>()).isEqualTo(1)
    }

    @Test
    fun `test saving pass owner into collection`() {
        mongoTemplate.insert(passOwnerFromDb)

        // GIVEN
        val changedFirstName = "Changed first name"
        val updatedPassOwner = passOwnerFromDb.copy(firstName = changedFirstName)
        val saved = passOwnerRepository.save(updatedPassOwner)

        // WHEN
        assertThat(saved.firstName).isEqualTo(changedFirstName)
    }

    @Test
    fun `test deleting pass from collection`() {
        mongoTemplate.insert(passOwnerFromDb)

        // GIVEN
        passOwnerRepository.deleteById(passOwnerIdFromDb)

        // WHEN
        assertFalse(
            message = "pass owner must not exist in collection after deletion",
            { mongoTemplate.exists<MongoPassOwner>(query(where("id").`is`(passOwnerFromDb))) }
        )
    }
}
