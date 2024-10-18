package com.example.passmanagersvc.service

import com.example.passmanagersvc.web.dto.PriceDistribution
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate

interface PassOwnerStatisticsService {
    fun calculateSpentAfterDate(afterDate: LocalDate, passOwnerId: String): Mono<BigDecimal>
    fun calculatePriceDistributions(passOwnerId: String): Flux<PriceDistribution>
}
