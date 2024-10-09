package com.example.passmanager.service

import com.example.passmanager.web.dto.PriceDistribution
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate

interface PassOwnerStatisticsService {
    fun calculateSpentAfterDate(afterDate: LocalDate, passOwnerId: String): Mono<BigDecimal>
    fun calculatePriceDistributions(passOwnerId: String): Flux<PriceDistribution>
}
