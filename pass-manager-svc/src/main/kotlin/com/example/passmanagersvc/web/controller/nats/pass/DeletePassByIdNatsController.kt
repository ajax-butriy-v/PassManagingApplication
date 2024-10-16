package com.example.passmanagersvc.web.controller.nats.pass

import com.example.internal.NatsSubject.Pass.DELETE_BY_ID
import com.example.internal.NatsSubject.Pass.PASS_QUEUE_GROUP
import com.example.passmanagersvc.input.reqreply.DeletePassByIdRequest
import com.example.passmanagersvc.input.reqreply.DeletePassByIdResponse
import com.example.passmanagersvc.service.PassService
import com.example.passmanagersvc.web.controller.nats.NatsController
import com.example.passmanagersvc.web.mapper.proto.pass.DeletePassByIdMapper
import com.google.protobuf.Parser
import io.nats.client.Connection
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

    override fun handle(request: DeletePassByIdRequest): Mono<DeletePassByIdResponse> {
        return passService.deleteById(request.id)
            .thenReturn(DeletePassByIdMapper.successDeletePassByIdResponse())
            .onErrorResume { thr -> DeletePassByIdMapper.failureDeletePassByIdResponse(thr).toMono() }
    }
}
