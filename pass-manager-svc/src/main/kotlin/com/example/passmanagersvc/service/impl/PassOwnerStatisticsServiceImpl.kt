package com.example.passmanagersvc.service.impl

import com.example.passmanagersvc.repositories.PassRepository
import com.example.passmanagersvc.service.PassOwnerService
import com.example.passmanagersvc.service.PassOwnerStatisticsService
import com.example.passmanagersvc.web.dto.PriceDistribution
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
