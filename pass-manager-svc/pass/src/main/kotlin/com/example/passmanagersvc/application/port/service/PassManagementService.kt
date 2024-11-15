package com.example.passmanagersvc.application.port.service

import com.example.internal.input.reqreply.TransferredPassStatisticsMessage
import com.example.passmanagersvc.application.port.input.PassManagementServiceInputPort
import com.example.passmanagersvc.application.port.input.PassOwnerServiceInputPort
import com.example.passmanagersvc.application.port.input.PassServiceInputPort
import com.example.passmanagersvc.application.port.input.PassTypeServiceInPort
import com.example.passmanagersvc.application.port.output.TransferPassMessageProducerOutPort
import com.example.passmanagersvc.application.port.output.TransferPassStatisticsMessageProducerOutPort
import com.example.passmanagersvc.domain.Pass
import com.example.passmanagersvc.domain.PassType
import com.example.passmanagersvc.infrastructure.kafka.mapper.TransferredPassStatisticsMessageMapper.toTransferPassStatisticsMessage
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import java.time.Duration
import java.time.Instant

@Service
class PassManagementService(
    private val passOwnerServiceInputPort: PassOwnerServiceInputPort,
    private val passServiceInputPort: PassServiceInputPort,
    private val transferPassMessageProducerOutPort: TransferPassMessageProducerOutPort,
    private val passTypeServiceInPort: PassTypeServiceInPort,
    private val transferPassStatisticsMessageProducerOutPort: TransferPassStatisticsMessageProducerOutPort,
) : PassManagementServiceInputPort {
    override fun cancelPass(passOwnerId: String, passId: String): Mono<Unit> {
        return passOwnerServiceInputPort.getById(passOwnerId).then(passServiceInputPort.deleteById(passId))
    }

    override fun transferPass(passId: String, targetPassOwnerId: String): Mono<Unit> {
        return Mono.zip(passServiceInputPort.getById(passId), passOwnerServiceInputPort.getById(targetPassOwnerId))
            .flatMap { (pass, passOwner) ->
                passServiceInputPort.update(pass.copy(passOwnerId = passOwner.id.toString())).map { it to passOwner.id }
            }
            .flatMap { (updatedPass, previousPassOwnerId) ->
                val key = updatedPass.passTypeId
                transferPassMessageProducerOutPort.sendTransferPassMessage(
                    updatedPass,
                    key,
                    previousPassOwnerId.toString()
                )
            }
            .thenReturn(Unit)
    }

    override fun publishTransferPassStatistics(pass: Pass, previousPassOwnerId: String): Mono<Unit> {
        return passTypeServiceInPort.getById(pass.passTypeId)
            .map { passType -> createTransferPassStatistics(passType, pass, previousPassOwnerId) }
            .flatMap { message ->
                transferPassStatisticsMessageProducerOutPort.sendTransferPassStatisticsMessage(
                    message,
                    message.passTypeId
                )
            }
            .thenReturn(Unit)
    }

    private fun createTransferPassStatistics(
        passType: PassType,
        pass: Pass,
        previousOwnerId: String,
    ): TransferredPassStatisticsMessage {
        val passTypePrice = passType.price
        val deltaBetweenPassAndPassTypePrices = passTypePrice - pass.purchasedFor
        val isDeltaPositive = deltaBetweenPassAndPassTypePrices.signum() > 0
        val durationUntilPassExpiration = Duration.between(Instant.now(), passType.activeTo)

        val transferStatisticsMessage = pass.toTransferPassStatisticsMessage(
            passType = passType,
            previousOwnerId = previousOwnerId,
            isDeltaPositive = isDeltaPositive,
            durationUntilPassExpiration = durationUntilPassExpiration
        )
        return transferStatisticsMessage
    }
}
