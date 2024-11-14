package com.example.passmanagersvc.application.port.output

import com.example.passmanagersvc.domain.Pass
import com.example.passmanagersvc.domain.PriceDistribution
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate

interface PassRepositoryOutPort {
    fun findByOwnerAndPurchasedAfter(passOwnerId: String, afterDate: LocalDate): Flux<Pass>
    fun findAllByPassOwnerId(passOwnerId: String): Flux<Pass>
    fun findById(passId: String): Mono<Pass>
    fun insert(newPass: Pass): Mono<Pass>
    fun save(pass: Pass): Mono<Pass>
    fun deleteById(passId: String): Mono<Unit>
    fun deleteAllByOwnerId(passOwnerId: String): Mono<Unit>
    fun sumPurchasedAtAfterDate(passOwnerId: String, afterDate: LocalDate): Mono<BigDecimal>
    fun getPassesPriceDistribution(passOwnerId: String): Flux<PriceDistribution>
}