package com.example.passmanagersvc.passowner.application.port.service

import com.example.passmanagersvc.passowner.application.port.input.PassOwnerServiceInputPort
import com.example.passmanagersvc.passowner.application.port.input.PassOwnerStatisticsServiceInputPort
import com.example.passmanagersvc.passowner.application.port.out.PassOwnerRepositoryOutPort
import com.example.passmanagersvc.passowner.domain.PriceDistribution
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate

@Service
class PassOwnerStatisticsService(
    private val passOwnerServiceInputPort: PassOwnerServiceInputPort,
    private val passOwnerRepositoryOutPort: PassOwnerRepositoryOutPort,
) : PassOwnerStatisticsServiceInputPort {
    override fun calculateSpentAfterDate(afterDate: LocalDate, passOwnerId: String): Mono<BigDecimal> {
        return passOwnerServiceInputPort.getById(passOwnerId)
            .then(passOwnerRepositoryOutPort.sumPurchasedAtAfterDate(passOwnerId, afterDate))
    }

    override fun calculatePriceDistributions(passOwnerId: String): Flux<PriceDistribution> {
        return passOwnerRepositoryOutPort.getPassesPriceDistribution(passOwnerId)
    }
}
