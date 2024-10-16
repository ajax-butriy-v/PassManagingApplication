package com.example.passmanagersvc.nats.controller.pass

import com.example.internal.NatsSubject.Pass.DELETE_BY_ID
import com.example.internal.input.reqreply.DeletePassByIdRequest
import com.example.internal.input.reqreply.DeletePassByIdResponse
import com.example.passmanagersvc.nats.controller.NatsController
import com.example.passmanagersvc.service.PassService
import com.example.passmanagersvc.web.mapper.proto.pass.DeletePassByIdMapper
import com.google.protobuf.Parser
import io.nats.client.Connection
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
class DeletePassByIdNatsController(
    override val connection: Connection,
    private val passService: PassService,
) : NatsController<DeletePassByIdRequest, DeletePassByIdResponse> {
    override val subject: String = DELETE_BY_ID
    override val queueGroup: String = PASS_QUEUE_GROUP
    override val parser: Parser<DeletePassByIdRequest> = DeletePassByIdRequest.parser()
    override val responseClass = DeletePassByIdResponse.getDefaultInstance()

    override fun handle(request: DeletePassByIdRequest): Mono<DeletePassByIdResponse> {
        return passService.deleteById(request.id)
            .thenReturn(DeletePassByIdMapper.successDeletePassByIdResponse())
            .onErrorResume {
                logger.error("Error occurred while executing", it)
                DeletePassByIdMapper.failureDeletePassByIdResponse(it).toMono()
            }
    }

    companion object {
        const val PASS_QUEUE_GROUP = "passQueueGroup"
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}
