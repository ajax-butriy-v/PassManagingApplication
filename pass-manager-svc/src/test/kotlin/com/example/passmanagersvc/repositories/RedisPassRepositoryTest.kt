package com.example.passmanagersvc.repositories

import com.example.passmanagersvc.dto.PriceDistribution
import com.example.passmanagersvc.repositories.impl.RedisPassRepository
import com.example.passmanagersvc.repositories.impl.RedisPassRepository.Companion.priceDistributionsKey
import com.example.passmanagersvc.util.IntegrationTest
import com.example.passmanagersvc.util.PassFixture.passTypes
import com.example.passmanagersvc.util.PassFixture.passesToCreate
import com.example.passmanagersvc.util.PassOwnerFixture.getOwnerWithUniqueFields
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

internal class RedisPassRepositoryTest : IntegrationTest() {
    @Autowired
    private lateinit var redisTemplate: ReactiveRedisTemplate<String, ByteArray>

    @Autowired
    private lateinit var mongoTemplate: ReactiveMongoTemplate

    @Autowired
    private lateinit var redisPassRepository: RedisPassRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `getting passes price distributions should return correct distribution per type`() {
        val insertedPassOwner = mongoTemplate.insert(getOwnerWithUniqueFields()).block()!!
        val passOwnerId = insertedPassOwner.id.toString()
        mongoTemplate.insertAll(passTypes).subscribe()
        mongoTemplate.insertAll(passesToCreate.map { it.copy(passOwnerId = insertedPassOwner.id) }).subscribe()

        // WHEN
        val priceDistributionFlux = redisPassRepository.getPassesPriceDistribution(passOwnerId)

        // THEN
        val key = priceDistributionsKey(passOwnerId)
        val doesCacheContainsListUnderKey = redisTemplate.opsForList().range(key, 0, -1)

        priceDistributionFlux.collectList()
            .test()
            .assertNext { priceDistributions ->
                assertThat(priceDistributions).hasSize(3).allMatch { it.spentForPassType == BigDecimal.TEN }
            }
            .verifyComplete()

        doesCacheContainsListUnderKey.test()
            .assertNext { byteArray ->
                val convertedResult = objectMapper.readValue<List<PriceDistribution>>(byteArray)
                assertThat(convertedResult).hasSize(3)
            }
            .verifyComplete()
    }

    @Test
    fun `getting sum of purchased passes for pass owner should return correct sum`() {
        // GIVEN
        val passOwner = mongoTemplate.insert(getOwnerWithUniqueFields()).block()!!
        val passOwnerId = passOwner.id.toString()
        val afterDate = LocalDate.now()
        passesToCreate.map { it.copy(passOwnerId = passOwner.id) }.also { mongoTemplate.insertAll(it).subscribe() }

        // WHEN
        val actualSum = redisPassRepository.sumPurchasedAtAfterDate(passOwnerId, afterDate)

        // THEN
        val doesCacheContainsBigDecimalUnderKey = redisTemplate.opsForValue().get(
            RedisPassRepository.purchaseAfterDateKey(
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
        val actualSum = redisPassRepository.sumPurchasedAtAfterDate(passOwnerId, afterDate)

        // THEN
        actualSum.test()
            .assertNext { assertThat(it).isEqualTo(BigDecimal.ZERO) }
            .verifyComplete()
    }
}
