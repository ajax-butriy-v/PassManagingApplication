package com.example.passmanagersvc.service

import com.example.passmanagersvc.domain.MongoPass
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

interface PassService {
    fun findById(passId: String): Mono<MongoPass>
    fun getById(passId: String): Mono<MongoPass>
    fun create(newPass: MongoPass, ownerId: String, passTypeId: String): Mono<MongoPass>
    fun update(pass: MongoPass): Mono<MongoPass>
    fun deleteById(passId: String): Mono<Unit>
    fun deleteAllByOwnerId(passOwnerId: String): Mono<Unit>
    fun findAllByPassOwnerAndPurchasedAtGreaterThan(passOwnerId: String, afterDate: LocalDate): Flux<MongoPass>
    fun findAllByPassOwnerId(passOwnerId: String): Flux<MongoPass>
}
