package com.example.passmanager.repositories

import com.example.passmanager.domain.MongoPassType
import reactor.core.publisher.Mono

interface PassTypeRepository {
    fun findById(passOwnerId: String): Mono<MongoPassType>
    fun insert(newMongoPassType: MongoPassType): Mono<MongoPassType>
    fun save(modifiedMongoPassType: MongoPassType): Mono<MongoPassType>
    fun deleteById(passOwnerId: String): Mono<Unit>
}
