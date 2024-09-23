package com.example.passmanager.testcontainers

import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@Import(MongoTestContainerConfiguration::class)
@ActiveProfiles("test")
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class WithMongoTestContainer
