package com.example.passmanager.service

import com.example.passmanager.web.dto.PriceDistribution
import java.math.BigDecimal
import java.time.LocalDate

interface PassOwnerStatisticsService {
    fun calculateSpentAfterDate(afterDate: LocalDate, passOwnerId: String): BigDecimal
    fun calculatePriceDistributions(passOwnerId: String): List<PriceDistribution>
}
