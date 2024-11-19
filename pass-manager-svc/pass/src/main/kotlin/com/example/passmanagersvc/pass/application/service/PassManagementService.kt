package com.example.passmanagersvc.pass.application.service

import com.example.internal.input.reqreply.TransferredPassStatisticsMessage
import com.example.passmanagersvc.pass.application.mapper.TransferredPassStatisticsMessageMapper.toTransferPassStatisticsMessage
import com.example.passmanagersvc.pass.application.port.input.PassManagementServiceInPort
import com.example.passmanagersvc.pass.application.port.input.PassServiceInPort
import com.example.passmanagersvc.pass.application.port.out.TransferPassMessageProducerOutPort
import com.example.passmanagersvc.pass.application.port.out.TransferPassStatisticsMessageProducerOutPort
import com.example.passmanagersvc.pass.domain.Pass
import com.example.passmanagersvc.passowner.application.port.input.PassOwnerServiceInPort
import com.example.passmanagersvc.passtype.application.port.input.PassTypeServiceInPort
import com.example.passmanagersvc.passtype.domain.PassType
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import java.time.Duration
import java.time.Instant

@Service
class PassManagementService(
    private val passOwnerServiceInPort: PassOwnerServiceInPort,
    private val passServiceInPort: PassServiceInPort,
    private val transferPassMessageProducerOutPort: TransferPassMessageProducerOutPort,
    private val passTypeServiceInPort: PassTypeServiceInPort,
    private val transferPassStatisticsMessageProducerOutPort: TransferPassStatisticsMessageProducerOutPort,
) : PassManagementServiceInPort {
    override fun cancelPass(passOwnerId: String, passId: String): Mono<Unit> {
        return passOwnerServiceInPort.getById(passOwnerId).then(passServiceInPort.deleteById(passId))
    }

    override fun transferPass(passId: String, targetPassOwnerId: String): Mono<Unit> {
        return Mono.zip(passServiceInPort.getById(passId), passOwnerServiceInPort.getById(targetPassOwnerId))
            .flatMap { (pass, passOwner) ->
                passServiceInPort.update(pass.copy(passOwnerId = passOwner.id.toString())).map { it to passOwner.id }
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
