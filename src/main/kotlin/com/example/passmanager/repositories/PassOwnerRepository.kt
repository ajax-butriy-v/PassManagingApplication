package com.example.passmanager.repositories

import com.example.passmanager.domain.MongoPassOwner
import reactor.core.publisher.Mono

interface PassOwnerRepository {
    fun findById(passOwnerId: String): Mono<MongoPassOwner>
    fun insert(newMongoPassOwner: MongoPassOwner): Mono<MongoPassOwner>
    fun deleteById(passOwnerId: String): Mono<Unit>
    fun save(newPassOwner: MongoPassOwner): Mono<MongoPassOwner>
}
