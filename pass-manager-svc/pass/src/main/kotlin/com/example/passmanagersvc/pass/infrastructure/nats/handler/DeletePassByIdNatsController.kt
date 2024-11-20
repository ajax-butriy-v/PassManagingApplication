package com.example.passmanagersvc.pass.infrastructure.nats.handler

import com.example.internal.NatsSubject.Pass.DELETE_BY_ID
import com.example.internal.input.reqreply.DeletePassByIdRequest
import com.example.internal.input.reqreply.DeletePassByIdResponse
import com.example.passmanagersvc.pass.application.port.input.PassServiceInPort
import com.example.passmanagersvc.pass.infrastructure.nats.mapper.DeletePassByIdMapper.failureDeletePassByIdResponse
import com.example.passmanagersvc.pass.infrastructure.nats.mapper.DeletePassByIdMapper.successDeletePassByIdResponse
import com.google.protobuf.Parser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import systems.ajax.nats.handler.api.ProtoNatsMessageHandler

@Component
class DeletePassByIdNatsController(
    private val passServiceInPort: PassServiceInPort,
) : ProtoNatsMessageHandler<DeletePassByIdRequest, DeletePassByIdResponse> {
    override val log: Logger = LoggerFactory.getLogger(DeletePassByIdNatsController::class.java)
    override val parser: Parser<DeletePassByIdRequest> = DeletePassByIdRequest.parser()
    override val queue: String? = PASS_QUEUE_GROUP
    override val subject: String = DELETE_BY_ID

    override fun doOnUnexpectedError(inMsg: DeletePassByIdRequest?, e: Exception): Mono<DeletePassByIdResponse> {
        return failureDeletePassByIdResponse(e).toMono()
    }

    override fun doHandle(inMsg: DeletePassByIdRequest): Mono<DeletePassByIdResponse> {
        return passServiceInPort.deleteById(inMsg.id)
            .thenReturn(successDeletePassByIdResponse())
    }

    companion object {
        const val PASS_QUEUE_GROUP = "passQueueGroup"
    }
}
