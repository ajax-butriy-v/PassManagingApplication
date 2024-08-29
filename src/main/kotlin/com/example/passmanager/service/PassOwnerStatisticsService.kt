package com.example.passmanager.service

import com.example.passmanager.web.dto.PriceDistribution
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.Instant

interface PassOwnerStatisticsService {
    fun calculateSpentAfterDate(afterDate: Instant, passOwnerId: ObjectId): BigDecimal
    fun calculatePriceDistributions(passOwnerId: ObjectId): List<PriceDistribution>
}
