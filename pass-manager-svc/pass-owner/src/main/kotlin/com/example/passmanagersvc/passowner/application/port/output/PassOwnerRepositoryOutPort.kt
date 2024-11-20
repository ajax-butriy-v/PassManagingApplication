package com.example.passmanagersvc.passowner.application.port.output

import com.example.passmanagersvc.passowner.domain.PassOwner
import com.example.passmanagersvc.passowner.domain.PriceDistribution
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate

interface PassOwnerRepositoryOutPort {
    fun findById(passOwnerId: String): Mono<PassOwner>
    fun insert(newPassOwner: PassOwner): Mono<PassOwner>
    fun deleteById(passOwnerId: String): Mono<Unit>
    fun save(newPassOwner: PassOwner): Mono<PassOwner>
    fun sumPurchasedAtAfterDate(passOwnerId: String, afterDate: LocalDate): Mono<BigDecimal>
    fun getPassesPriceDistribution(passOwnerId: String): Flux<PriceDistribution>
}
