package com.example.passmanagersvc.pass.infrastructure.nats.handler

import com.example.internal.NatsSubject.Pass.TRANSFER
import com.example.internal.input.reqreply.TransferPassRequest
import com.example.internal.input.reqreply.TransferPassResponse
import com.example.passmanagersvc.pass.application.port.input.PassManagementServiceInputPort
import com.example.passmanagersvc.pass.infrastructure.nats.mapper.TransferPassMapper.failureTransferPassResponse
import com.example.passmanagersvc.pass.infrastructure.nats.mapper.TransferPassMapper.successTransferPassResponse
import com.google.protobuf.Parser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import systems.ajax.nats.handler.api.ProtoNatsMessageHandler

@Component
class TransferPassNatsController(
    private val passManagementServiceInputPort: PassManagementServiceInputPort,
) : ProtoNatsMessageHandler<TransferPassRequest, TransferPassResponse> {
    override val log: Logger = LoggerFactory.getLogger(TransferPassNatsController::class.java)
    override val parser: Parser<TransferPassRequest> = TransferPassRequest.parser()
    override val queue: String = PASS_QUEUE_GROUP
    override val subject: String = TRANSFER

    override fun doOnUnexpectedError(inMsg: TransferPassRequest?, e: Exception): Mono<TransferPassResponse> {
        return failureTransferPassResponse(e).toMono()
    }

    override fun doHandle(inMsg: TransferPassRequest): Mono<TransferPassResponse> {
        return passManagementServiceInputPort.transferPass(inMsg.id, inMsg.ownerId)
            .thenReturn(successTransferPassResponse())
    }

    companion object {
        const val PASS_QUEUE_GROUP = "passQueueGroup"
    }
}
