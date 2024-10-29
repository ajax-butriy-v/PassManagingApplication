package com.example.passmanagersvc.service.impl

import com.example.internal.input.reqreply.TransferredPassStatisticsMessage
import com.example.passmanagersvc.domain.MongoPass
import com.example.passmanagersvc.domain.MongoPassType
import com.example.passmanagersvc.kafka.producer.TransferPassStatisticsMessageProducer
import com.example.passmanagersvc.repositories.PassRepository
import com.example.passmanagersvc.service.PassOwnerService
import com.example.passmanagersvc.service.PassOwnerStatisticsService
import com.example.passmanagersvc.service.PassTypeService
import com.example.passmanagersvc.web.dto.PriceDistribution
import com.example.passmanagersvc.web.mapper.proto.pass.TransferredPassStatisticsMessageMapper.toTransferPassStatisticsMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate

@Service
internal class PassOwnerStatisticsServiceImpl(
    private val passOwnerService: PassOwnerService,
    private val passRepository: PassRepository,
    private val passTypeService: PassTypeService,
    private val transferPassStatisticsMessageProducer: TransferPassStatisticsMessageProducer,
) : PassOwnerStatisticsService {
    override fun calculateSpentAfterDate(afterDate: LocalDate, passOwnerId: String): Mono<BigDecimal> {
        return passOwnerService.getById(passOwnerId)
            .then(passRepository.sumPurchasedAtAfterDate(passOwnerId, afterDate))
    }

    override fun calculatePriceDistributions(passOwnerId: String): Flux<PriceDistribution> {
        return passRepository.getPassesPriceDistribution(passOwnerId)
    }

    override fun publishTransferPassStatistics(pass: MongoPass, previousPassOwnerId: String): Mono<Unit> {
        return passTypeService.getById(pass.passTypeId.toString())
            .map { passType -> mapToStatisticsWithPassTypeId(passType, pass, previousPassOwnerId) }
            .flatMap { (message, passTypeId) ->
                transferPassStatisticsMessageProducer.sendTransferPassStatisticsMessage(message, passTypeId)
            }
            .thenReturn(Unit)
    }

    fun mapToStatisticsWithPassTypeId(
        passType: MongoPassType,
        pass: MongoPass,
        previousOwnerId: String,
    ): Pair<TransferredPassStatisticsMessage, String> {
        val passTypePrice = passType.price ?: BigDecimal.ZERO
        val deltaBetweenPassAndPassTypePrices = passTypePrice - (pass.purchasedFor ?: BigDecimal.ZERO)
        val isDeltaPositive = deltaBetweenPassAndPassTypePrices.signum() > 0

        val transferStatisticsMessage = pass.toTransferPassStatisticsMessage(
            passType = passType,
            previousOwnerId = previousOwnerId,
            isDeltaPositive = isDeltaPositive
        )
        log.info("Statistics message {}", transferStatisticsMessage)
        return transferStatisticsMessage to passType.id.toString()
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }
}
