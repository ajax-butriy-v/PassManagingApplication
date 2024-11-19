package com.example.passmanagersvc.passowner.application.service

import com.example.passmanagersvc.passowner.application.port.input.PassOwnerServiceInPort
import com.example.passmanagersvc.passowner.application.port.input.PassOwnerStatisticsServiceInPort
import com.example.passmanagersvc.passowner.application.port.out.PassOwnerRepositoryOutPort
import com.example.passmanagersvc.passowner.domain.PriceDistribution
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate

@Service
class PassOwnerStatisticsService(
    private val passOwnerServiceInPort: PassOwnerServiceInPort,
    private val passOwnerRepositoryOutPort: PassOwnerRepositoryOutPort,
) : PassOwnerStatisticsServiceInPort {
    override fun calculateSpentAfterDate(afterDate: LocalDate, passOwnerId: String): Mono<BigDecimal> {
        return passOwnerServiceInPort.getById(passOwnerId)
            .then(passOwnerRepositoryOutPort.sumPurchasedAtAfterDate(passOwnerId, afterDate))
    }

    override fun calculatePriceDistributions(passOwnerId: String): Flux<PriceDistribution> {
        return passOwnerRepositoryOutPort.getPassesPriceDistribution(passOwnerId)
    }
}
