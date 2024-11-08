package com.example.passmanagersvc.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
class RedisConfiguration(
    @Value("\${spring.data.redis.timeout.millis}")
    private val redisCommandTimeoutInMillis: Long,
    @Value("\${spring.data.redis.port}")
    private val redisPort: Int,
    @Value("\${spring.data.redis.host}")
    private val redisHost: String,
) {

    @Bean
    @Primary
    fun reactiveRedisConnectionFactory(): ReactiveRedisConnectionFactory {
        val lettuceClientConfiguration = LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ofMillis(redisCommandTimeoutInMillis))
            .build()
        val serverConfig = RedisStandaloneConfiguration(redisHost, redisPort)
        return LettuceConnectionFactory(serverConfig, lettuceClientConfiguration)
    }

    @Bean
    fun reactiveRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, ByteArray> {
        val context = RedisSerializationContext.newSerializationContext<String, ByteArray>(StringRedisSerializer())
            .value(RedisSerializer.byteArray())
            .build()
        return ReactiveRedisTemplate(factory, context)
    }
}
