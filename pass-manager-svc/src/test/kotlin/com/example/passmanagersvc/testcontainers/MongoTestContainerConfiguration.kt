package com.example.passmanagersvc.testcontainers

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.MongoDBContainer

@TestConfiguration(proxyBeanMethods = false)
class MongoTestContainerConfiguration {
    @Bean
    @ServiceConnection
    fun mongoContainer(): MongoDBContainer = MongoDBContainer("mongo").apply { start() }
}
