package com.example.passmanagersvc.application.port.input

import com.example.passmanagersvc.domain.Pass
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

interface PassServiceInputPort {
    fun findById(passId: String): Mono<Pass>
    fun getById(passId: String): Mono<Pass>
    fun create(newPass: Pass, ownerId: String, passTypeId: String): Mono<Pass>
    fun update(pass: Pass): Mono<Pass>
    fun deleteById(passId: String): Mono<Unit>
    fun deleteAllByOwnerId(passOwnerId: String): Mono<Unit>
    fun findAllByPassOwnerAndPurchasedAtGreaterThan(passOwnerId: String, afterDate: LocalDate): Flux<Pass>
    fun findAllByPassOwnerId(passOwnerId: String): Flux<Pass>
}
