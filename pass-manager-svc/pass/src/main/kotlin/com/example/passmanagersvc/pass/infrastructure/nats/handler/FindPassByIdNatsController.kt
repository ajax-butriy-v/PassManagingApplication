package com.example.passmanagersvc.pass.infrastructure.nats.handler

import com.example.internal.NatsSubject.Pass.FIND_BY_ID
import com.example.internal.input.reqreply.FindPassByIdRequest
import com.example.internal.input.reqreply.FindPassByIdResponse
import com.example.passmanagersvc.pass.application.port.input.PassServiceInputPort
import com.example.passmanagersvc.pass.infrastructure.nats.mapper.FindPassByIdMapper.failureFindByIdPassResponse
import com.example.passmanagersvc.pass.infrastructure.nats.mapper.FindPassByIdMapper.toSuccessFindPassByIdResponse
import com.example.passmanagersvc.pass.infrastructure.nats.mapper.ProtoPassMapper.toProto
import com.google.protobuf.Parser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import systems.ajax.nats.handler.api.ProtoNatsMessageHandler

@Component
class FindPassByIdNatsController(
    private val passServiceInputPort: PassServiceInputPort,
) : ProtoNatsMessageHandler<FindPassByIdRequest, FindPassByIdResponse> {
    override val log: Logger = LoggerFactory.getLogger(FindPassByIdNatsController::class.java)
    override val parser: Parser<FindPassByIdRequest> = FindPassByIdRequest.parser()
    override val queue: String = PASS_QUEUE_GROUP
    override val subject: String = FIND_BY_ID

    override fun doOnUnexpectedError(inMsg: FindPassByIdRequest?, e: Exception): Mono<FindPassByIdResponse> {
        return failureFindByIdPassResponse(e).toMono()
    }

    override fun doHandle(inMsg: FindPassByIdRequest): Mono<FindPassByIdResponse> {
        return passServiceInputPort.getById(inMsg.id)
            .map { passFromDb -> passFromDb.toProto() }
            .map { proto -> proto.toSuccessFindPassByIdResponse() }
    }

    companion object {
        const val PASS_QUEUE_GROUP = "passQueueGroup"
    }
}
