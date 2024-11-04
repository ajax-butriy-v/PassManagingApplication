package com.example.passmanagersvc.nats.controller.pass

import com.example.internal.NatsSubject.Pass.FIND_BY_ID
import com.example.internal.input.reqreply.FindPassByIdRequest
import com.example.internal.input.reqreply.FindPassByIdResponse
import com.example.passmanagersvc.mapper.proto.pass.FindPassByIdMapper.failureFindByIdPassResponse
import com.example.passmanagersvc.mapper.proto.pass.FindPassByIdMapper.toProto
import com.example.passmanagersvc.mapper.proto.pass.FindPassByIdMapper.toSuccessFindPassByIdResponse
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
class FindPassByIdNatsController(
    override val connection: Connection,
    private val passService: PassService,
) : NatsController<FindPassByIdRequest, FindPassByIdResponse> {

    override val subject: String = FIND_BY_ID
    override val queueGroup: String = PASS_QUEUE_GROUP
    override val parser: Parser<FindPassByIdRequest> = FindPassByIdRequest.parser()
    override val responseClass = FindPassByIdResponse.getDefaultInstance()

    override fun handle(request: FindPassByIdRequest): Mono<FindPassByIdResponse> {
        return passService.getById(request.id)
            .map { passFromDb -> passFromDb.toProto() }
            .map { proto -> proto.toSuccessFindPassByIdResponse() }
            .onErrorResume {
                logger.error("Error occurred while executing", it)
                failureFindByIdPassResponse(it).toMono()
            }
    }

    companion object {
        const val PASS_QUEUE_GROUP = "passQueueGroup"
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}
