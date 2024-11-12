package com.example.passmanagersvc

import com.example.passmanagersvc.configuration.RedisProperties
import io.mongock.runner.springboot.EnableMongock
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableMongock
@EnableConfigurationProperties(RedisProperties::class)
internal class PassManagingApplication

@SuppressWarnings("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<PassManagingApplication>(*args)
}
