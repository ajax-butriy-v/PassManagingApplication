package com.example.passmanagersvc.repositories

import com.example.passmanagersvc.domain.MongoPassType
import reactor.core.publisher.Mono

interface PassTypeRepository {
    fun findById(passOwnerId: String): Mono<MongoPassType>
    fun insert(newMongoPassType: MongoPassType): Mono<MongoPassType>
    fun save(modifiedMongoPassType: MongoPassType): Mono<MongoPassType>
    fun deleteById(passOwnerId: String): Mono<Unit>
}
