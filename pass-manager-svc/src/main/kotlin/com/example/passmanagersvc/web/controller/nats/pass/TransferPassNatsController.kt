package com.example.passmanagersvc.web.controller.nats.pass

import com.example.internal.NatsSubject.Pass.PASS_QUEUE_GROUP
import com.example.internal.NatsSubject.Pass.TRANSFER
import com.example.internal.input.reqreply.TransferPassRequest
import com.example.internal.input.reqreply.TransferPassResponse
import com.example.passmanagersvc.service.PassManagementService
import com.example.passmanagersvc.web.controller.nats.NatsController
import com.example.passmanagersvc.web.mapper.proto.pass.TransferPassMapper.failureTransferPassResponse
import com.example.passmanagersvc.web.mapper.proto.pass.TransferPassMapper.successTransferPassResponse
import com.google.protobuf.Parser
import io.nats.client.Connection
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

    override fun handle(request: TransferPassRequest): Mono<TransferPassResponse> {
        return passManagementService.transferPass(request.id, request.ownerId)
            .thenReturn(successTransferPassResponse())
            .onErrorResume { throwable ->
                failureTransferPassResponse(throwable).toMono()
            }
    }
}
