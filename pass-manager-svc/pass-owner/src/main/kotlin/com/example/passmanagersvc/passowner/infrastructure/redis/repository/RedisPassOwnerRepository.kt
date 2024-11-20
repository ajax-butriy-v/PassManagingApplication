package com.example.passmanagersvc.passowner.infrastructure.redis.repository

import com.example.core.exception.PassOwnerNotFoundException
import com.example.core.util.isRedisOrSocketException
import com.example.passmanagersvc.passowner.application.port.output.PassOwnerRepositoryOutPort
import com.example.passmanagersvc.passowner.domain.PassOwner
import com.example.passmanagersvc.passowner.domain.PriceDistribution
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
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

@Primary
@Repository
class RedisPassOwnerRepository(
    @Value("\${spring.data.redis.ttl.minutes}")
    private val redisExpirationTimeoutInMinutes: Long,
    @Qualifier("mongoPassOwnerRepository")
    private val mongoPassOwnerRepository: PassOwnerRepositoryOutPort,
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, ByteArray>,
    private val objectMapper: ObjectMapper,
) : PassOwnerRepositoryOutPort by mongoPassOwnerRepository {

    override fun findById(passOwnerId: String): Mono<PassOwner> {
        val key = passOwnerKey(passOwnerId)
        return reactiveRedisTemplate.opsForValue().get(key)
            .handle { item, sink ->
                if (item.isEmpty()) {
                    sink.error(PassOwnerNotFoundException("Could not find pass owner by id $passOwnerId"))
                } else {
                    sink.next(item)
                }
            }
            .map { objectMapper.readValue<PassOwner>(it) }
            .switchIfEmpty { findInMongoAndWriteToRedis(passOwnerId, key) }
            .onErrorResume(::isRedisOrSocketException) { mongoPassOwnerRepository.findById(passOwnerId) }
    }

    private fun findInMongoAndWriteToRedis(
        passOwnerId: String,
        key: String,
    ) = mongoPassOwnerRepository.findById(passOwnerId)
        .flatMap(::savePassOwnerToRedis)
        .switchIfEmpty {
            reactiveRedisTemplate.opsForValue()
                .set(key, byteArrayOf(), Duration.ofMinutes(redisExpirationTimeoutInMinutes))
                .then(Mono.empty())
        }

    override fun insert(newPassOwner: PassOwner): Mono<PassOwner> {
        return mongoPassOwnerRepository.insert(newPassOwner).flatMap { created ->
            savePassOwnerToRedis(created)
                .onErrorResume(::isRedisOrSocketException) {
                    log.warn("Redis failed to cache created user.", it)
                    Mono.empty()
                }.thenReturn(created)
        }
    }

    override fun deleteById(passOwnerId: String): Mono<Unit> {
        val key = passOwnerKey(passOwnerId)
        return mongoPassOwnerRepository.deleteById(passOwnerId)
            .flatMap {
                reactiveRedisTemplate.opsForValue().delete(key)
                    .onErrorResume(::isRedisOrSocketException) {
                        log.warn("Redis failed to remove user from cache.", it)
                        Mono.empty()
                    }.thenReturn(Unit)
            }
    }

    override fun save(newPassOwner: PassOwner): Mono<PassOwner> {
        return mongoPassOwnerRepository.save(newPassOwner).flatMap { saved ->
            savePassOwnerToRedis(saved).onErrorResume(::isRedisOrSocketException) {
                log.warn("Redis failed to cache created user.", it)
                Mono.empty()
            }.thenReturn(saved)
        }
    }

    override fun sumPurchasedAtAfterDate(passOwnerId: String, afterDate: LocalDate): Mono<BigDecimal> {
        val key = purchaseAfterDateKey(passOwnerId, afterDate)
        return reactiveRedisTemplate.opsForValue().get(key)
            .map { objectMapper.readValue<BigDecimal>(it) }
            .switchIfEmpty {
                mongoPassOwnerRepository.sumPurchasedAtAfterDate(passOwnerId, afterDate)
                    .flatMap { purchasedAfterDate ->
                        val byteArray = objectMapper.writeValueAsBytes(purchasedAfterDate)
                        reactiveRedisTemplate.opsForValue()
                            .set(key, byteArray, Duration.ofMinutes(redisExpirationTimeoutInMinutes))
                            .thenReturn(purchasedAfterDate)
                    }
            }
            .onErrorResume(::isRedisOrSocketException) {
                mongoPassOwnerRepository.sumPurchasedAtAfterDate(passOwnerId, afterDate)
            }
    }

    override fun getPassesPriceDistribution(passOwnerId: String): Flux<PriceDistribution> {
        val key = priceDistributionsKey(passOwnerId)
        return reactiveRedisTemplate.opsForList().range(key, 0, -1)
            .flatMap { objectMapper.readValue<List<PriceDistribution>>(it).toFlux() }
            .switchIfEmptyDeferred {
                mongoPassOwnerRepository.getPassesPriceDistribution(passOwnerId)
                    .collectList()
                    .flatMapMany { addPriceDistributionsToRedis(it, key) }
            }
            .onErrorResume(::isRedisOrSocketException) {
                mongoPassOwnerRepository.getPassesPriceDistribution(passOwnerId)
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

    private fun savePassOwnerToRedis(passOwner: PassOwner): Mono<PassOwner> {
        val key = passOwnerKey(passOwner.id.toString())
        val byteArray = objectMapper.writeValueAsBytes(passOwner)
        return reactiveRedisTemplate.opsForValue().set(
            key,
            byteArray,
            Duration.ofMinutes(redisExpirationTimeoutInMinutes)
        ).thenReturn(passOwner)
    }

    companion object {
        private const val KEY_PREFIX = "key-pass-owner"
        private val log = LoggerFactory.getLogger(RedisPassOwnerRepository::class.java)

        fun passOwnerKey(passOwnerId: String): String {
            return "$KEY_PREFIX$passOwnerId"
        }

        fun purchaseAfterDateKey(passOwnerId: String, afterDate: LocalDate): String {
            return "$KEY_PREFIX${RedisPassOwnerRepository::sumPurchasedAtAfterDate.name}-$passOwnerId-$afterDate"
        }

        fun priceDistributionsKey(passOwnerId: String): String {
            return "$KEY_PREFIX${RedisPassOwnerRepository::getPassesPriceDistribution.name}-$passOwnerId"
        }
    }
}
