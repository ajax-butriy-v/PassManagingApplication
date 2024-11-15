package com.example.passmanagersvc.infrastructure.nats.handler

import com.example.internal.NatsSubject.Pass.CREATE
import com.example.internal.input.reqreply.CreatePassRequest
import com.example.internal.input.reqreply.CreatePassResponse
import com.example.passmanagersvc.application.port.input.PassServiceInputPort
import com.example.passmanagersvc.infrastructure.kafka.mapper.TransferredPassMessageMapper.toDomain
import com.example.passmanagersvc.infrastructure.kafka.mapper.TransferredPassMessageMapper.toProto
import com.example.passmanagersvc.infrastructure.nats.mapper.CreatePassMapper.failureCreatedPassResponse
import com.example.passmanagersvc.infrastructure.nats.mapper.CreatePassMapper.toSuccessCreatePassResponse
import com.google.protobuf.Parser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import systems.ajax.nats.handler.api.ProtoNatsMessageHandler

@Component
class CreatePassNatsController(
    private val passServiceInputPort: PassServiceInputPort,
) : ProtoNatsMessageHandler<CreatePassRequest, CreatePassResponse> {
    override val log: Logger = LoggerFactory.getLogger(CreatePassNatsController::class.java)
    override val parser: Parser<CreatePassRequest> = CreatePassRequest.parser()
    override val queue: String? = PASS_QUEUE_GROUP
    override val subject: String = CREATE

    override fun doOnUnexpectedError(inMsg: CreatePassRequest?, e: Exception): Mono<CreatePassResponse> {
        return failureCreatedPassResponse(e).toMono()
    }

    override fun doHandle(inMsg: CreatePassRequest): Mono<CreatePassResponse> {
        return inMsg.passToCreate.let { protoPass ->
            passServiceInputPort.create(protoPass.toDomain(), protoPass.passOwnerId, protoPass.passTypeId)
                .map { createdPass -> createdPass.toProto() }
                .map { proto -> proto.toSuccessCreatePassResponse() }
        }
    }

    companion object {
        const val PASS_QUEUE_GROUP = "passQueueGroup"
    }
}