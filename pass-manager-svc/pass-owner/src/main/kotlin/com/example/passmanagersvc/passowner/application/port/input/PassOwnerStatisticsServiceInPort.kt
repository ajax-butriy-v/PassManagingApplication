package com.example.passmanagersvc.passowner.application.port.input

import com.example.passmanagersvc.passowner.domain.PriceDistribution
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate

interface PassOwnerStatisticsServiceInPort {
    fun calculateSpentAfterDate(afterDate: LocalDate, passOwnerId: String): Mono<BigDecimal>
    fun calculatePriceDistributions(passOwnerId: String): Flux<PriceDistribution>
}
