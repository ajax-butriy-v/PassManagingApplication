package com.example.passmanagersvc.nats.controller.pass

import com.example.internal.NatsSubject.Pass.TRANSFER
import com.example.internal.input.reqreply.TransferPassRequest
import com.example.internal.input.reqreply.TransferPassResponse
import com.example.passmanagersvc.mapper.proto.pass.TransferPassMapper.failureTransferPassResponse
import com.example.passmanagersvc.mapper.proto.pass.TransferPassMapper.successTransferPassResponse
import com.example.passmanagersvc.nats.controller.NatsController
import com.example.passmanagersvc.service.PassManagementService
import com.google.protobuf.Parser
import io.nats.client.Connection
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
class TransferPassNatsController(
    override val connection: Connection,
    private val passManagementService: PassManagementService,
) : NatsController<TransferPassRequest, TransferPassResponse> {
    override val subject: String = TRANSFER
    override val queueGroup: String = PASS_QUEUE_GROUP
    override val parser: Parser<TransferPassRequest> = TransferPassRequest.parser()
    override val responseClass = TransferPassResponse.getDefaultInstance()

    override fun handle(request: TransferPassRequest): Mono<TransferPassResponse> {
        return passManagementService.transferPass(request.id, request.ownerId)
            .thenReturn(successTransferPassResponse())
            .onErrorResume {
                logger.error("Error occurred while executing", it)
                failureTransferPassResponse(it).toMono()
            }
    }

    companion object {
        const val PASS_QUEUE_GROUP = "passQueueGroup"
        private val logger: Logger = LoggerFactory.getLogger(TransferPassNatsController::class.java)
    }
}
