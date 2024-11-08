package com.example.passmanagersvc.repositories

import com.example.passmanagersvc.domain.MongoPassOwner
import com.example.passmanagersvc.repositories.impl.RedisPassOwnerRepository
import com.example.passmanagersvc.repositories.impl.RedisPassOwnerRepository.Companion.passOwnerKey
import com.example.passmanagersvc.util.IntegrationTest
import com.example.passmanagersvc.util.PassOwnerFixture.getOwnerWithUniqueFields
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.ReactiveRedisTemplate
import reactor.kotlin.test.test
import kotlin.test.Test

internal class RedisPassOwnerRepositoryTest : IntegrationTest() {
    @Autowired
    private lateinit var redisTemplate: ReactiveRedisTemplate<String, ByteArray>

    @Autowired
    private lateinit var redisPassOwnerRepository: RedisPassOwnerRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `finding pass owner by id should return pass owner by id from cache`() {
        // GIVEN
        val insertedPassOwner = redisPassOwnerRepository.insert(getOwnerWithUniqueFields()).block()!!
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
        val ownerToCreate = getOwnerWithUniqueFields()
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
        val insertedPassOwner = redisPassOwnerRepository.insert(getOwnerWithUniqueFields()).block()!!
        val changedFirstName = "Changed first name"
        val updatedPassOwner = insertedPassOwner.copy(firstName = changedFirstName)

        // WHEN
        val savedPassOwner = redisPassOwnerRepository.save(updatedPassOwner)

        // WHEN
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
        val insertedPassOwner = redisPassOwnerRepository.insert(getOwnerWithUniqueFields()).block()!!
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
}
