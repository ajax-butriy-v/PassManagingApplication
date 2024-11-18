package com.example.passmanagersvc.passowner.redis.repository

import com.example.passmanagersvc.passowner.infrastructure.mongo.entity.MongoPassOwner
import com.example.passmanagersvc.passowner.infrastructure.mongo.mapper.PassOwnerMapper.toDomain
import com.example.passmanagersvc.passowner.infrastructure.redis.repository.RedisPassOwnerRepository
import com.example.passmanagersvc.passowner.infrastructure.redis.repository.RedisPassOwnerRepository.Companion.passOwnerKey
import com.example.passmanagersvc.passowner.infrastructure.redis.repository.RedisPassOwnerRepository.Companion.purchaseAfterDateKey
import com.example.passmanagersvc.passowner.util.IntegrationTest
import com.example.passmanagersvc.util.PassFixture.passesToCreate
import com.example.passmanagersvc.util.PassOwnerFixture.getOwnerWithUniqueFields
import com.example.passmanagersvc.util.PassOwnerFixture.mongoPassOwnerToCreate
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.redis.core.ReactiveRedisTemplate
import reactor.kotlin.test.test
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.Test

internal class RedisPassOwnerRepositoryTest : IntegrationTest() {
    @Autowired
    private lateinit var redisTemplate: ReactiveRedisTemplate<String, ByteArray>

    @Autowired
    private lateinit var redisPassOwnerRepository: RedisPassOwnerRepository

    @Autowired
    private lateinit var mongoTemplate: ReactiveMongoTemplate

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `finding pass owner by id should return pass owner by id from cache`() {
        // GIVEN
        val ownerToCreate = getOwnerWithUniqueFields().toDomain()
        val insertedPassOwner = redisPassOwnerRepository.insert(ownerToCreate).block()!!
        val insertedPassOwnerId = insertedPassOwner.id.toString()
        val byteArray = objectMapper.writeValueAsBytes(insertedPassOwner)
        redisTemplate.opsForValue().set(passOwnerKey(insertedPassOwnerId), byteArray).block()!!

        // WHEN
        val passOwnerById = redisPassOwnerRepository.findById(insertedPassOwnerId)

        // THEN
        val doesCacheContainsPassOwner = redisTemplate.hasKey(passOwnerKey(insertedPassOwner.id.toString()))

        passOwnerById.test()
            .assertNext { assertThat(it).isEqualTo(insertedPassOwner) }
            .verifyComplete()

        doesCacheContainsPassOwner.test()
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `inserting pass owner in collection should return created pass owner and save it to cache`() {
        // GIVEN
        val ownerToCreate = getOwnerWithUniqueFields().toDomain()
        val insertedPassOwner = redisPassOwnerRepository.insert(ownerToCreate).block()!!
        val insertedPassOwnerId = insertedPassOwner.id.toString()

        // WHEN
        val passOwnerById = redisPassOwnerRepository.findById(insertedPassOwnerId)

        // THEN
        val doesCacheContainsPassOwner = redisTemplate.hasKey(passOwnerKey(insertedPassOwnerId))

        passOwnerById.test()
            .assertNext { assertThat(it).isEqualTo(insertedPassOwner) }
            .verifyComplete()

        doesCacheContainsPassOwner.test()
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `saving pass owner in collection show update existing pass owner by id and update cache`() {
        // GIVEN
        val ownerToCreate = getOwnerWithUniqueFields().toDomain()
        val insertedPassOwner = redisPassOwnerRepository.insert(ownerToCreate).block()!!
        val changedFirstName = "Changed first name"
        val updatedPassOwner = insertedPassOwner.copy(firstName = changedFirstName)

        // WHEN
        val savedPassOwner = redisPassOwnerRepository.save(updatedPassOwner)

        // THEN
        val passOwnerInCache = redisTemplate.opsForValue().get(passOwnerKey(insertedPassOwner.id.toString()))

        savedPassOwner.test()
            .assertNext { assertThat(it.firstName).isEqualTo(changedFirstName) }
            .verifyComplete()

        passOwnerInCache.test()
            .assertNext { byteArray ->
                val convertedResult = objectMapper.readValue<MongoPassOwner>(byteArray)
                assertThat(convertedResult.firstName).isEqualTo(updatedPassOwner.firstName)
            }
            .verifyComplete()
    }

    @Test
    fun `deleting pass owner by id should delete pass owner from collection and delete it from cache`() {
        // GIVEN
        val ownerToCreate = getOwnerWithUniqueFields().toDomain()
        val insertedPassOwner = redisPassOwnerRepository.insert(ownerToCreate).block()!!
        val insertedPassOwnerId = insertedPassOwner.id.toString()

        // WHEN
        val deleteResult = redisPassOwnerRepository.deleteById(insertedPassOwnerId)

        // THEN
        val doesCacheContainsPassOwner = redisTemplate.hasKey(passOwnerKey(insertedPassOwnerId))

        deleteResult.test()
            .expectNext(Unit)
            .verifyComplete()

        doesCacheContainsPassOwner.test()
            .expectNext(false)
            .verifyComplete()
    }

    @Test
    fun `getting sum of purchased passes for pass owner should return correct sum`() {
        // GIVEN
        val passOwner = mongoTemplate.insert(mongoPassOwnerToCreate).block()!!
        val passOwnerId = passOwner.id.toString()
        mongoTemplate.insertAll(passesToCreate(passOwner.id!!)).collectList().block()!!
        val afterDate = LocalDate.now()

        // WHEN
        val actualSum = redisPassOwnerRepository.sumPurchasedAtAfterDate(passOwnerId, afterDate)

        // THEN
        val doesCacheContainsBigDecimalUnderKey = redisTemplate.opsForValue().get(
            purchaseAfterDateKey(
                passOwnerId,
                afterDate
            )
        )

        val expectedValue = BigDecimal.valueOf(30)

        actualSum.test()
            .assertNext { assertThat(it).isEqualTo(expectedValue) }
            .verifyComplete()

        doesCacheContainsBigDecimalUnderKey.test()
            .assertNext { byteArray ->
                val convertedValue = objectMapper.readValue<BigDecimal>(byteArray)
                assertThat(convertedValue).isEqualTo(expectedValue)
            }
            .verifyComplete()
    }

    @Test
    fun `getting sum of purchased passes for pass owner with no passes should return zero sum`() {
        // GIVEN
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields()).block()!!
        val passOwnerId = insertedPassOwner.id.toString()
        val afterDate = LocalDate.now()

        // WHEN
        val actualSum = redisPassOwnerRepository.sumPurchasedAtAfterDate(passOwnerId, afterDate)

        // THEN
        actualSum.test()
            .assertNext { assertThat(it).isEqualTo(BigDecimal.ZERO) }
            .verifyComplete()
    }
}
