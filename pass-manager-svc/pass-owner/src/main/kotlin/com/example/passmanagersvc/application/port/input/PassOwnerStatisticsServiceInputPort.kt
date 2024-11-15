package com.example.passmanagersvc.application.port.input

import com.example.passmanagersvc.domain.PriceDistribution
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate

interface PassOwnerStatisticsServiceInputPort {
    fun calculateSpentAfterDate(afterDate: LocalDate, passOwnerId: String): Mono<BigDecimal>
    fun calculatePriceDistributions(passOwnerId: String): Flux<PriceDistribution>
}
