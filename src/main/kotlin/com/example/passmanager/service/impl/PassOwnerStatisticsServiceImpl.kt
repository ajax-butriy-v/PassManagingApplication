package com.example.passmanager.service.impl

import com.example.passmanager.repositories.PassRepository
import com.example.passmanager.service.PassOwnerService
import com.example.passmanager.service.PassOwnerStatisticsService
import com.example.passmanager.web.dto.PriceDistribution
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate

@Service
internal class PassOwnerStatisticsServiceImpl(
    private val passOwnerService: PassOwnerService,
    private val passRepository: PassRepository
) : PassOwnerStatisticsService {
    override fun calculateSpentAfterDate(afterDate: LocalDate, passOwnerId: String): Mono<BigDecimal> {
        return passOwnerService.getById(passOwnerId)
            .then(passRepository.sumPurchasedAtAfterDate(passOwnerId, afterDate))
    }

    override fun calculatePriceDistributions(passOwnerId: String): Flux<PriceDistribution> {
        return passRepository.getPassesPriceDistribution(passOwnerId)
    }
}
