package com.example.core.util

import io.lettuce.core.RedisCommandTimeoutException
import io.lettuce.core.RedisException
import org.springframework.data.redis.RedisConnectionFailureException
import java.net.SocketException

fun isRedisOrSocketException(error: Throwable): Boolean {
    return listOf(
        RedisConnectionFailureException::class.java,
        RedisException::class.java,
        SocketException::class.java,
        RedisCommandTimeoutException::class.java
    ).any { it.isInstance(error) }
}
