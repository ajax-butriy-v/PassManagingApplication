package com.example.passmanagersvc.pass.application.port.output

import com.example.passmanagersvc.pass.domain.Pass
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

interface PassRepositoryOutPort {
    fun findByOwnerAndPurchasedAfter(passOwnerId: String, afterDate: LocalDate): Flux<Pass>
    fun findAllByPassOwnerId(passOwnerId: String): Flux<Pass>
    fun findById(passId: String): Mono<Pass>
    fun insert(newPass: Pass): Mono<Pass>
    fun save(pass: Pass): Mono<Pass>
    fun deleteById(passId: String): Mono<Unit>
    fun deleteAllByOwnerId(passOwnerId: String): Mono<Unit>
}
