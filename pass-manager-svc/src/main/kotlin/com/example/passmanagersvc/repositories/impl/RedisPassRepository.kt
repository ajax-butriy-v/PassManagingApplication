package com.example.passmanagersvc.repositories.impl

import com.example.core.util.isRedisOrSocketException
import com.example.passmanagersvc.dto.PriceDistribution
import com.example.passmanagersvc.repositories.PassRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.switchIfEmptyDeferred
import reactor.kotlin.core.publisher.toFlux
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDate

@Repository
class RedisPassRepository(
    @Value("\${spring.data.redis.ttl.minutes}")
    private val redisExpirationTimeoutInMinutes: Long,
    private val mongoPassRepository: MongoPassRepository,
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, ByteArray>,
    private val objectMapper: ObjectMapper,
) : PassRepository by mongoPassRepository {

    override fun sumPurchasedAtAfterDate(passOwnerId: String, afterDate: LocalDate): Mono<BigDecimal> {
        val key = purchaseAfterDateKey(passOwnerId, afterDate)
        return reactiveRedisTemplate.opsForValue().get(key)
            .map { objectMapper.readValue<BigDecimal>(it) }
            .switchIfEmpty {
                mongoPassRepository.sumPurchasedAtAfterDate(passOwnerId, afterDate)
                    .flatMap { purchasedAfterDate ->
                        val byteArray = objectMapper.writeValueAsBytes(purchasedAfterDate)
                        reactiveRedisTemplate.opsForValue()
                            .set(key, byteArray, Duration.ofMinutes(redisExpirationTimeoutInMinutes))
                            .thenReturn(purchasedAfterDate)
                    }
            }
            .onErrorResume(::isRedisOrSocketException) {
                mongoPassRepository.sumPurchasedAtAfterDate(passOwnerId, afterDate)
            }
    }

    override fun getPassesPriceDistribution(passOwnerId: String): Flux<PriceDistribution> {
        val key = priceDistributionsKey(passOwnerId)
        return reactiveRedisTemplate.opsForList().range(key, 0, -1)
            .flatMap { objectMapper.readValue<List<PriceDistribution>>(it).toFlux() }
            .switchIfEmptyDeferred {
                mongoPassRepository.getPassesPriceDistribution(passOwnerId)
                    .collectList()
                    .flatMapMany { addPriceDistributionsToRedis(it, key) }
            }
            .onErrorResume(::isRedisOrSocketException) {
                mongoPassRepository.getPassesPriceDistribution(passOwnerId)
            }
    }

    private fun addPriceDistributionsToRedis(
        priceDistributions: List<PriceDistribution>,
        key: String,
    ): Flux<PriceDistribution> {
        val byteArray = objectMapper.writeValueAsBytes(priceDistributions)
        val durationInSeconds = Duration.ofMinutes(redisExpirationTimeoutInMinutes).seconds
        val redisScript = RedisScript.of<Unit>(ClassPathResource("scripts/rpush_and_expire.lua"))

        return reactiveRedisTemplate.execute(
            redisScript,
            listOf(key),
            listOf(byteArray, durationInSeconds.toString().toByteArray())
        ).thenMany(priceDistributions.toFlux())
    }

    companion object {
        private const val KEY_PREFIX = "key-"

        fun purchaseAfterDateKey(passOwnerId: String, afterDate: LocalDate): String {
            return "$KEY_PREFIX${RedisPassRepository::sumPurchasedAtAfterDate.name}-$passOwnerId-$afterDate"
        }

        fun priceDistributionsKey(passOwnerId: String): String {
            return "$KEY_PREFIX${RedisPassRepository::getPassesPriceDistribution.name}-$passOwnerId"
        }
    }
}
