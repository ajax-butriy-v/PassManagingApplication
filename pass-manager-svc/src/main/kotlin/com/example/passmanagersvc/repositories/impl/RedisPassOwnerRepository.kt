package com.example.passmanagersvc.repositories.impl

import com.example.core.exception.PassOwnerNotFoundException
import com.example.core.util.isRedisOrSocketException
import com.example.passmanagersvc.domain.MongoPassOwner
import com.example.passmanagersvc.repositories.PassOwnerRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.Duration

@Repository
class RedisPassOwnerRepository(
    @Value("\${spring.data.redis.ttl.minutes}")
    private val redisExpirationTimeoutInMinutes: Long,
    private val mongoPassOwnerRepository: MongoPassOwnerRepository,
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, ByteArray>,
    private val objectMapper: ObjectMapper,
) : PassOwnerRepository by mongoPassOwnerRepository {

    override fun findById(passOwnerId: String): Mono<MongoPassOwner> {
        val key = passOwnerKey(passOwnerId)
        return reactiveRedisTemplate.opsForValue().get(key)
            .handle { item, sink ->
                if (item.isEmpty()) {
                    sink.error(PassOwnerNotFoundException("Could not find pass owner by id $passOwnerId"))
                } else {
                    sink.next(item)
                }
            }
            .map { objectMapper.readValue<MongoPassOwner>(it) }
            .switchIfEmpty {
                mongoPassOwnerRepository.findById(passOwnerId)
                    .flatMap(::savePassOwnerToRedis)
                    .switchIfEmpty {
                        reactiveRedisTemplate.opsForValue().set(key, byteArrayOf()).then(Mono.empty())
                    }
            }
            .onErrorResume({ isRedisOrSocketException(it) }, { mongoPassOwnerRepository.findById(passOwnerId) })
    }

    override fun insert(newMongoPassOwner: MongoPassOwner): Mono<MongoPassOwner> {
        return mongoPassOwnerRepository.insert(newMongoPassOwner).flatMap { created ->
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

    override fun save(newPassOwner: MongoPassOwner): Mono<MongoPassOwner> {
        return mongoPassOwnerRepository.save(newPassOwner).flatMap { saved ->
            savePassOwnerToRedis(saved).onErrorResume(::isRedisOrSocketException) {
                log.warn("Redis failed to cache created user.", it)
                Mono.empty()
            }.thenReturn(saved)
        }
    }

    private fun savePassOwnerToRedis(passOwner: MongoPassOwner): Mono<MongoPassOwner> {
        val key = passOwnerKey(passOwner.id.toString())
        val byteArray = objectMapper.writeValueAsBytes(passOwner)
        return reactiveRedisTemplate.opsForValue().set(
            key,
            byteArray,
            Duration.ofMinutes(redisExpirationTimeoutInMinutes)
        ).thenReturn(passOwner)
    }

    companion object {
        private const val KEY_PREFIX = "key-"
        private val log = LoggerFactory.getLogger(RedisPassOwnerRepository::class.java)

        fun passOwnerKey(passOwnerId: String): String {
            return "$KEY_PREFIX$passOwnerId"
        }
    }
}
