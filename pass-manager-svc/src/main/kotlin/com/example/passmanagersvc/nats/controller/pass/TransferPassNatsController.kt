package com.example.passmanagersvc.nats.controller.pass

import com.example.internal.NatsSubject.Pass.TRANSFER
import com.example.internal.input.reqreply.TransferPassRequest
import com.example.internal.input.reqreply.TransferPassResponse
import com.example.passmanagersvc.mapper.proto.pass.TransferPassMapper.failureTransferPassResponse
import com.example.passmanagersvc.mapper.proto.pass.TransferPassMapper.successTransferPassResponse
import com.example.passmanagersvc.service.PassManagementService
import com.google.protobuf.Parser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import systems.ajax.nats.handler.api.ProtoNatsMessageHandler

@Component
class TransferPassNatsController(
    private val passManagementService: PassManagementService,
) : ProtoNatsMessageHandler<TransferPassRequest, TransferPassResponse> {
    override val log: Logger = LoggerFactory.getLogger(TransferPassNatsController::class.java)
    override val parser: Parser<TransferPassRequest> = TransferPassRequest.parser()
    override val queue: String = PASS_QUEUE_GROUP
    override val subject: String = TRANSFER

    override fun doOnUnexpectedError(inMsg: TransferPassRequest?, e: Exception): Mono<TransferPassResponse> {
        return failureTransferPassResponse(e).toMono()
    }

    override fun doHandle(inMsg: TransferPassRequest): Mono<TransferPassResponse> {
        return passManagementService.transferPass(inMsg.id, inMsg.ownerId)
            .thenReturn(successTransferPassResponse())
    }

    companion object {
        const val PASS_QUEUE_GROUP = "passQueueGroup"
    }
}
