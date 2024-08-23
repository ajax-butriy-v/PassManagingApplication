package com.example.pass_manager.service.impl

import com.example.pass_manager.exception.PassOwnerNotFoundException
import com.example.pass_manager.service.PassOwnerService
import com.example.pass_manager.service.PassOwnerStatisticsService
import com.example.pass_manager.service.PassService
import com.example.pass_manager.web.dto.PriceDistribution
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant

@Service
class PassOwnerStatisticsServiceImpl(
    private val passOwnerService: PassOwnerService,
    private val passService: PassService,
) : PassOwnerStatisticsService {
    override fun calculateSpentAfterDate(afterDate: Instant, passOwnerId: ObjectId): BigDecimal {
        val clientInDb = passOwnerService.findById(passOwnerId)
        return clientInDb?.let { client ->
            val ownedPassesAfterDate = passService.findAllByPassOwnerAndPurchasedAtGreaterThan(client, afterDate)
            ownedPassesAfterDate.map { it.purchasedFor ?: BigDecimal.ZERO }.sumOf { it }
        } ?: throw PassOwnerNotFoundException(passOwnerId)
    }

    override fun calculatePriceDistributions(passOwnerId: ObjectId): List<PriceDistribution> {
        val passTypeWithTotalMap = passService.findAllByPassOwnerId(passOwnerId)
            .map { pass -> pass.passType to pass.purchasedFor }
            .groupBy({ (passType, _) -> passType?.name }, { it.second })
            .mapValues { (_, prices) -> prices.sumOf { price -> price ?: BigDecimal.ZERO } }

        return passTypeWithTotalMap.map { PriceDistribution(it.key, it.value) }
    }
}

