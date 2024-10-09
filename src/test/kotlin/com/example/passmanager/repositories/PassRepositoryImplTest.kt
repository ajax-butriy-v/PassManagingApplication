package com.example.passmanager.repositories

import com.example.passmanager.domain.MongoPass
import com.example.passmanager.testcontainers.WithMongoTestContainer
import com.example.passmanager.util.PassFixture.passToCreate
import com.example.passmanager.util.PassFixture.passTypes
import com.example.passmanager.util.PassFixture.passesToCreate
import com.example.passmanager.util.PassOwnerFixture.getOwnerWithUniqueFields
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
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

@SpringBootTest
@WithMongoTestContainer
internal class PassRepositoryImplTest {

    @Autowired
    private lateinit var mongoTemplate: ReactiveMongoTemplate

    @Autowired
    private lateinit var passRepository: PassRepository

    @Test
    fun `finding pass by existing id should return pass by id`() {
        // GIVEN
        val insertedPass = mongoTemplate.insert(passToCreate).block()

        // WHEN
        val passById = passRepository.findById(insertedPass!!.id.toString())

        // THEN
        passById.test()
            .assertNext {
                assertThat(it).isEqualTo(insertedPass)
            }
            .verifyComplete()
    }

    @Test
    fun `inserting pass in collection should return created pass`() {
        // GIVEN
        val insertedPass = mongoTemplate.insert(passToCreate).block()

        // WHEN
        val passById = mongoTemplate.findById<MongoPass>(insertedPass!!.id.toString())

        // THEN
        assertThat(insertedPass).isEqualTo(passToCreate.copy(id = insertedPass.id, version = insertedPass.version))

        passById.test()
            .assertNext {
                assertThat(it).isEqualTo(insertedPass)
            }
            .verifyComplete()
    }

    @Test
    fun `saving pass in collection show update existing pass by id`() {
        // GIVEN
        val insertedPass = mongoTemplate.insert(passToCreate)
        val changedPrice = BigDecimal.TEN
        val updatedPass = insertedPass.map { it.copy(purchasedFor = changedPrice) }

        // WHEN
        val saved = updatedPass.flatMap { passRepository.save(it) }

        // WHEN
        saved.test()
            .assertNext { assertThat(it.purchasedFor).isEqualTo(changedPrice) }
            .verifyComplete()
    }

    @Test
    fun `deleting pass by id should delete pass from collection`() {
        // GIVEN
        val insertedPass = mongoTemplate.insert(passToCreate).block()
        val insertedPassId = insertedPass!!.id

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
                        pass.purchasedAt?.isAfter(dateToInstant) ?: false
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

    @Test
    fun `getting passes price distributions should return correct distribution per type`() {
        // GIVEN
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields()).block()
        mongoTemplate.insertAll(passTypes).subscribe()
        mongoTemplate.insertAll(passesToCreate.map { it.copy(passOwnerId = insertedPassOwner!!.id) }).subscribe()

        // WHEN
        val priceDistributionFlux = passRepository.getPassesPriceDistribution(insertedPassOwner!!.id.toString())

        // THEN
        priceDistributionFlux.collectList()
            .test()
            .assertNext { priceDistributions ->
                assertThat(priceDistributions).hasSize(3).allMatch { it.spentForPassType == BigDecimal.TEN }
            }
            .verifyComplete()
    }

    @Test
    fun `getting sum of purchased passes for pass owner should return correct sum`() {
        // GIVEN
        val passOwner = mongoTemplate.insert(getOwnerWithUniqueFields()).block()
        val passOwnerId = passOwner!!.id
        val afterDate = LocalDate.now()
        passesToCreate.map { it.copy(passOwnerId = passOwnerId) }.also { mongoTemplate.insertAll(it).subscribe() }

        // WHEN
        val sum = passRepository.sumPurchasedAtAfterDate(passOwnerId.toString(), afterDate)

        // THEN
        sum.test()
            .assertNext { assertThat(it).isEqualTo(BigDecimal.valueOf(30)) }
            .verifyComplete()
    }

    @Test
    fun `getting sum of purchased passes for pass owner with no passes should return zero sum`() {
        // GIVEN
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields())
        val afterDate = LocalDate.now()

        // WHEN
        val sum = insertedPassOwner
            .mapNotNull { it.id.toString() }
            .flatMap { passRepository.sumPurchasedAtAfterDate(it, afterDate) }

        // THEN
        sum.test()
            .assertNext { assertThat(it).isEqualTo(BigDecimal.ZERO) }
            .verifyComplete()
    }
}
