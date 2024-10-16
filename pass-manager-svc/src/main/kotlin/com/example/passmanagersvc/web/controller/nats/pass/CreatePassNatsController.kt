package com.example.passmanagersvc.web.controller.nats.pass

import com.example.internal.NatsSubject.Pass.CREATE
import com.example.internal.NatsSubject.Pass.PASS_QUEUE_GROUP
import com.example.internal.input.reqreply.CreatePassRequest
import com.example.internal.input.reqreply.CreatePassResponse
import com.example.passmanagersvc.service.PassService
import com.example.passmanagersvc.web.controller.nats.NatsController
import com.example.passmanagersvc.web.mapper.proto.pass.CreatePassMapper.failureCreatedPassResponse
import com.example.passmanagersvc.web.mapper.proto.pass.CreatePassMapper.toModel
import com.example.passmanagersvc.web.mapper.proto.pass.CreatePassMapper.toSuccessCreatePassResponse
import com.example.passmanagersvc.web.mapper.proto.pass.FindPassByIdMapper.toProto
import com.google.protobuf.Parser
import io.nats.client.Connection
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
class CreatePassNatsController(
    override val connection: Connection,
    private val passService: PassService,
) : NatsController<CreatePassRequest, CreatePassResponse> {
    override val subject: String = CREATE
    override val queueGroup: String = PASS_QUEUE_GROUP
    override val parser: Parser<CreatePassRequest> = CreatePassRequest.parser()

    override fun handle(request: CreatePassRequest): Mono<CreatePassResponse> {
        return passService.create(request.toModel(), request.passOwnerId, request.passTypeId)
            .map { createdPass -> createdPass.toProto() }
            .map { proto -> proto.toSuccessCreatePassResponse() }
            .onErrorResume { throwable ->
                failureCreatedPassResponse(throwable).toMono()
            }
    }
}
