package com.example.passmanagersvc.infrastructure.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.data.redis")
data class RedisProperties(
    val timeout: Timeout,
    val ttl: Ttl,
    val port: Int,
    val host: String,
) {
    data class Timeout(val millis: Long)
    data class Ttl(val minutes: Long)
}
