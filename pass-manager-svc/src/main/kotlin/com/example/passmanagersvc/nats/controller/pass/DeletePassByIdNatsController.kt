package com.example.passmanagersvc.nats.controller.pass

import com.example.internal.NatsSubject.Pass.DELETE_BY_ID
import com.example.internal.input.reqreply.DeletePassByIdRequest
import com.example.internal.input.reqreply.DeletePassByIdResponse
import com.example.passmanagersvc.mapper.proto.pass.DeletePassByIdMapper
import com.example.passmanagersvc.service.PassService
import com.google.protobuf.Parser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import systems.ajax.nats.handler.api.ProtoNatsMessageHandler

@Component
class DeletePassByIdNatsController(
    private val passService: PassService,
) : ProtoNatsMessageHandler<DeletePassByIdRequest, DeletePassByIdResponse> {
    override val log: Logger = LoggerFactory.getLogger(DeletePassByIdNatsController::class.java)
    override val parser: Parser<DeletePassByIdRequest> = DeletePassByIdRequest.parser()
    override val queue: String? = PASS_QUEUE_GROUP
    override val subject: String = DELETE_BY_ID

    override fun doOnUnexpectedError(inMsg: DeletePassByIdRequest?, e: Exception): Mono<DeletePassByIdResponse> {
        log.error("Error occurred while executing", e)
        return DeletePassByIdMapper.failureDeletePassByIdResponse(e).toMono()
    }

    override fun doHandle(inMsg: DeletePassByIdRequest): Mono<DeletePassByIdResponse> {
        return passService.deleteById(inMsg.id)
            .thenReturn(DeletePassByIdMapper.successDeletePassByIdResponse())
    }

    companion object {
        const val PASS_QUEUE_GROUP = "passQueueGroup"
    }
}
