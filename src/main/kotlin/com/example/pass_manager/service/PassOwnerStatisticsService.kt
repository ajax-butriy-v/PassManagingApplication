package com.example.pass_manager.service

import com.example.pass_manager.web.dto.PriceDistribution
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.Instant

interface PassOwnerStatisticsService {
    fun calculateSpentAfterDate(afterDate: Instant, passOwnerId: ObjectId): BigDecimal
    fun calculatePriceDistributions(passOwnerId: ObjectId): List<PriceDistribution>
}

