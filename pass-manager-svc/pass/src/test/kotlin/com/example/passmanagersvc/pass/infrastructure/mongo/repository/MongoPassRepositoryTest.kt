package com.example.passmanagersvc.pass.infrastructure.mongo.repository

import com.example.passmanagersvc.pass.infrastructure.mongo.entity.MongoPass
import com.example.passmanagersvc.pass.infrastructure.mongo.mapper.PassMapper.toDomain
import com.example.passmanagersvc.pass.util.IntegrationTest
import com.example.passmanagersvc.util.PassFixture.mongoPassToCreate
import com.example.passmanagersvc.util.PassFixture.passesToCreate
import com.example.passmanagersvc.util.PassOwnerFixture.getOwnerWithUniqueFields
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Fields
import org.springframework.data.mongodb.core.exists
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.isEqualTo
import reactor.kotlin.test.test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertFalse

internal class MongoPassRepositoryTest : IntegrationTest() {

    @Autowired
    private lateinit var mongoTemplate: ReactiveMongoTemplate

    @Autowired
    private lateinit var passRepository: MongoPassRepository

    @Test
    fun `finding pass by existing id should return pass by id`() {
        // GIVEN
        val insertedPass = mongoTemplate.insert(mongoPassToCreate).block()!!

        // WHEN
        val passById = passRepository.findById(insertedPass.id.toString())

        // THEN
        passById.test()
            .assertNext {
                assertThat(it).isEqualTo(insertedPass.toDomain())
            }
            .verifyComplete()
    }

    @Test
    fun `inserting pass in collection should return created pass`() {
        // GIVEN
        val insertedPass = mongoTemplate.insert(mongoPassToCreate).block()!!

        // WHEN
        val passById = mongoTemplate.findById<MongoPass>(insertedPass.id.toString())

        // THEN
        assertThat(insertedPass).isEqualTo(
            mongoPassToCreate.copy(
                id = insertedPass.id,
                version = insertedPass.version
            )
        )

        passById.test()
            .assertNext {
                assertThat(it).isEqualTo(insertedPass)
            }
            .verifyComplete()
    }

    @Test
    fun `saving pass in collection show update existing pass by id`() {
        // GIVEN
        val insertedPass = mongoTemplate.insert(mongoPassToCreate).block()!!
        val changedPrice = BigDecimal.TEN
        val updatedPass = insertedPass.copy(purchasedFor = changedPrice)

        // WHEN
        val saved = passRepository.save(updatedPass.toDomain())

        // WHEN
        saved.test()
            .assertNext { assertThat(it.purchasedFor).isEqualTo(changedPrice) }
            .verifyComplete()
    }

    @Test
    fun `deleting pass by id should delete pass from collection`() {
        // GIVEN
        val insertedPass = mongoTemplate.insert(mongoPassToCreate).block()!!
        val insertedPassId = insertedPass.id

        // WHEN
        val delete = passRepository.deleteById(insertedPassId.toString())

        // THEN
        delete.test()
            .expectNext(Unit)
            .verifyComplete()

        assertFalse("pass must not exist in collection after deletion") {
            val existsById = mongoTemplate.exists<MongoPass>(
                query(where(Fields.UNDERSCORE_ID).isEqualTo(insertedPassId))
            )
            existsById.block() == true
        }
    }

    @Test
    fun `deleting all passes by owner id should delete all owner's passes`() {
        // GIVEN
        val passOwner = mongoTemplate.insert(getOwnerWithUniqueFields()).block()
        val passOwnerId = passOwner!!.id
        passesToCreate.map { it.copy(passOwnerId = passOwnerId) }.also { mongoTemplate.insertAll(it).subscribe() }

        // WHEN
        val deleteAll = passRepository.deleteAllByOwnerId(passOwnerId.toString())

        // THEN`
        deleteAll.test()
            .expectNext(Unit)
            .verifyComplete()

        assertFalse("all owner passes must not exist in db after deletion") {
            val existsByOwnerid = mongoTemplate.exists<MongoPass>(
                query(where(MongoPass::passOwnerId.name).isEqualTo(passOwnerId.toString()))
            )
            existsByOwnerid.block() == true
        }
    }

    @Test
    fun `finding by owner and purchased after should return corresponding passes`() {
        // GIVEN
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields())
        insertedPassOwner
            .map { owner -> passesToCreate.map { it.copy(passOwnerId = owner.id) } }
            .map { passes -> mongoTemplate.insertAll(passes) }
            .subscribe()
        val afterDate = LocalDate.now()

        // WHEN
        val passes = insertedPassOwner
            .mapNotNull { it.id.toString() }
            .flatMapMany { passRepository.findByOwnerAndPurchasedAfter(it, afterDate) }

        // THEN
        passes.collectList()
            .test()
            .assertNext {
                assertThat(it).hasSize(3)
                    .allMatch { pass ->
                        val dateToInstant = afterDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
                        pass.purchasedAt.isAfter(dateToInstant)
                    }
            }
    }

    @Test
    fun `finding all by pass owner id should return corresponding passes`() {
        // GIVEN
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields())
        insertedPassOwner
            .map { owner -> passesToCreate.map { it.copy(passOwnerId = owner.id) } }
            .map { passes -> mongoTemplate.insertAll(passes) }
            .subscribe()

        // WHEN
        val passesByOwnerId = insertedPassOwner
            .mapNotNull { it.id.toString() }
            .flatMapMany { passRepository.findAllByPassOwnerId(it) }

        // THEN
        passesByOwnerId.collectList()
            .test()
            .assertNext { passes ->
                val passOwnerId = passes.first().passOwnerId
                assertThat(passes).hasSize(3).allMatch { it.passOwnerId == passOwnerId }
            }
    }

    @Test
    fun `finding all by non-existent pass owner id should return empty list`() {
        // WHEN
        val passesByOwnerId = passRepository.findAllByPassOwnerId(ObjectId.get().toString())

        // THEN
        passesByOwnerId.collectList()
            .test()
            .assertNext { assertThat(it).isEmpty() }
            .verifyComplete()
    }
}
