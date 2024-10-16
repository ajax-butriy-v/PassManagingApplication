package com.example.passmanagersvc.repositories

import com.example.passmanagersvc.domain.MongoPass
import com.example.passmanagersvc.web.dto.PriceDistribution
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate

interface PassRepository {
    fun findByOwnerAndPurchasedAfter(passOwnerId: String, afterDate: LocalDate): Flux<MongoPass>
    fun findAllByPassOwnerId(passOwnerId: String): Flux<MongoPass>
    fun findById(passId: String): Mono<MongoPass>
    fun insert(newPass: MongoPass): Mono<MongoPass>
    fun save(pass: MongoPass): Mono<MongoPass>
    fun deleteById(passId: String): Mono<Unit>
    fun deleteAllByOwnerId(passOwnerId: String): Mono<Unit>
    fun sumPurchasedAtAfterDate(passOwnerId: String, afterDate: LocalDate): Mono<BigDecimal>
    fun getPassesPriceDistribution(passOwnerId: String): Flux<PriceDistribution>
}
