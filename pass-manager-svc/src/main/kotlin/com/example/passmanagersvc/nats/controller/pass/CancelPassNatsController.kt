package com.example.passmanagersvc.nats.controller.pass

import com.example.internal.NatsSubject.Pass.CANCEL
import com.example.internal.input.reqreply.CancelPassRequest
import com.example.internal.input.reqreply.CancelPassResponse
import com.example.passmanagersvc.mapper.proto.pass.CancelPassMapper.failureCancelPassResponse
import com.example.passmanagersvc.mapper.proto.pass.CancelPassMapper.successCancelPassResponse
import com.example.passmanagersvc.service.PassManagementService
import com.google.protobuf.Parser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import systems.ajax.nats.handler.api.ProtoNatsMessageHandler

@Component
class CancelPassNatsController(
    private val passManagementService: PassManagementService,
) : ProtoNatsMessageHandler<CancelPassRequest, CancelPassResponse> {
    override val log: Logger = LoggerFactory.getLogger(CancelPassNatsController::class.java)
    override val parser: Parser<CancelPassRequest> = CancelPassRequest.parser()
    override val queue: String = PASS_QUEUE_GROUP
    override val subject = CANCEL

    override fun doOnUnexpectedError(inMsg: CancelPassRequest?, e: Exception): Mono<CancelPassResponse> {
        return failureCancelPassResponse(e).toMono()
    }

    override fun doHandle(inMsg: CancelPassRequest): Mono<CancelPassResponse> {
        return passManagementService.cancelPass(inMsg.ownerId, inMsg.id)
            .thenReturn(successCancelPassResponse())
    }

    companion object {
        const val PASS_QUEUE_GROUP = "passQueueGroup"
    }
}
