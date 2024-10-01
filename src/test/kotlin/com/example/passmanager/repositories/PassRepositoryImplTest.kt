package com.example.passmanager.repositories

import com.example.passmanager.domain.MongoPass
import com.example.passmanager.testcontainers.WithMongoTestContainer
import com.example.passmanager.util.PassFixture.passToCreate
import com.example.passmanager.util.PassFixture.passTypesToCreate
import com.example.passmanager.util.PassFixture.passesToCreate
import com.example.passmanager.util.PassOwnerFixture.getOwnerWithUniqueFields
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
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
import kotlin.test.assertNotNull

@SpringBootTest
@WithMongoTestContainer
class PassRepositoryImplTest {

    @Autowired
    private lateinit var mongoTemplate: ReactiveMongoTemplate

    @Autowired
    private lateinit var passRepository: PassRepository

    @Test
    fun `finding pass by existing id should return pass by id`() {
        // GIVEN
        val insertedPass = mongoTemplate.insert(passToCreate)

        // WHEN
        val passById = insertedPass
            .mapNotNull { it.id.toString() }
            .flatMap { insertedPassId -> passRepository.findById(insertedPassId) }

        // THEN
        passById.test()
            .assertNext { assertNotNull(it) }
            .verifyComplete()
    }

    @Test
    fun `inserting pass in collection should return created pass`() {
        // WHEN
        val insertedPass = mongoTemplate.insert(passToCreate)

        // THEN
        insertedPass
            .mapNotNull { it.id }
            .test()
            .assertNext { passId ->
                mongoTemplate.findById<MongoPass>(passId!!)
                    .mapNotNull { it.id }
                    .doOnNext { id -> assertThat(passId).isEqualTo(id) }
                    .subscribe()
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
        // Cache the result to avoid re-triggering insert
        val insertedPass = mongoTemplate.insert(passToCreate).cache()

        // WHEN
        insertedPass
            .mapNotNull { it.id.toString() }
            .flatMap { passId -> passRepository.deleteById(passId) }
            .subscribe()

        // THEN
        insertedPass
            .mapNotNull { it.id }
            .flatMap { mongoTemplate.exists<MongoPass>(query(where("_id").isEqualTo(it))) }
            .test()
            .assertNext { exists -> assertFalse("pass must not exist in collection after deletion") { exists } }
            .verifyComplete()
    }

    @Test
    fun `deleting all passes by owner id should delete all owner's passes`() {
        // GIVEN
        // Cache the result to avoid re-triggering insert
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields()).cache()
        insertedPassOwner
            .map { owner -> passesToCreate.map { it.copy(passOwnerId = owner.id) } }
            .map { passes -> mongoTemplate.insertAll(passes) }
            .subscribe()

        // WHEN
        val monoPassOwnerId = insertedPassOwner.mapNotNull { it.id.toString() }
        monoPassOwnerId.flatMap { passRepository.deleteAllByOwnerId(it) }.subscribe()

        // THEN
        monoPassOwnerId.flatMap { mongoTemplate.exists<MongoPass>(query(where("passOwnerId").isEqualTo(it))) }
            .test()
            .assertNext { exists -> assertFalse("all owner passes must not exist in db after deletion") { exists } }
            .verifyComplete()
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
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields())
        val insertedPassTypes = mongoTemplate.insertAll(passTypesToCreate)
        insertedPassOwner.mapNotNull { it.id }
            .flatMapMany { ownerId ->
                insertedPassTypes.map {
                    passToCreate.copy(
                        passTypeId = it.id,
                        passOwnerId = ownerId
                    )
                }
            }
            .collectList()
            .map { mongoTemplate.insertAll(it) }
            .subscribe()

        // WHEN
        val priceDistributionFlux = insertedPassOwner.mapNotNull { it.id.toString() }
            .flatMapMany { passRepository.getPassesPriceDistribution(it) }

        // THEN
        priceDistributionFlux.collectList()
            .test()
            .assertNext { priceDistributions ->
                assertThat(priceDistributions).hasSize(3).allMatch { it.spentForPassType == BigDecimal.TEN }
            }
    }

    @Test
    fun `getting sum of purchased passes for pass owner should return correct sum`() {
        // GIVEN
        // Cache the result to avoid re-triggering insert
        val passOwnerMono = mongoTemplate.insert(getOwnerWithUniqueFields()).cache()
        passOwnerMono.subscribe()

        passOwnerMono
            .map { owner -> passesToCreate.map { it.copy(passOwnerId = owner.id) } }
            .flatMapMany { passes -> mongoTemplate.insertAll(passes) }
            .subscribe()

        val afterDate = LocalDate.now()

        // WHEN
        val sum = passOwnerMono
            .mapNotNull { it.id.toString() }
            .flatMap { passRepository.sumPurchasedAtAfterDate(it, afterDate) }

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
