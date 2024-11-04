package com.example.passmanagersvc.nats.controller.pass

import com.example.internal.NatsSubject.Pass.CREATE
import com.example.internal.input.reqreply.CreatePassRequest
import com.example.internal.input.reqreply.CreatePassResponse
import com.example.passmanagersvc.mapper.proto.pass.CreatePassMapper.failureCreatedPassResponse
import com.example.passmanagersvc.mapper.proto.pass.CreatePassMapper.toModel
import com.example.passmanagersvc.mapper.proto.pass.CreatePassMapper.toSuccessCreatePassResponse
import com.example.passmanagersvc.mapper.proto.pass.FindPassByIdMapper.toProto
import com.example.passmanagersvc.nats.controller.NatsController
import com.example.passmanagersvc.service.PassService
import com.google.protobuf.Parser
import io.nats.client.Connection
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
    override val responseClass = CreatePassResponse.getDefaultInstance()

    override fun handle(request: CreatePassRequest): Mono<CreatePassResponse> {
        return request.passToCreate.let { protoPass ->
            passService.create(protoPass.toModel(), protoPass.passOwnerId, protoPass.passTypeId)
                .map { createdPass -> createdPass.toProto() }
                .map { proto -> proto.toSuccessCreatePassResponse() }
                .onErrorResume {
                    logger.error("Error occurred in NATS controller. ", it)
                    failureCreatedPassResponse(it).toMono()
                }
        }
    }

    companion object {
        const val PASS_QUEUE_GROUP = "passQueueGroup"
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}
