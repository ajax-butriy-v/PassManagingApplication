package com.example.passmanagersvc.web.controller.nats.pass

import com.example.internal.NatsSubject.Pass.CANCEL
import com.example.internal.NatsSubject.Pass.PASS_QUEUE_GROUP
import com.example.internal.input.reqreply.CancelPassRequest
import com.example.internal.input.reqreply.CancelPassResponse
import com.example.passmanagersvc.service.PassManagementService
import com.example.passmanagersvc.web.controller.nats.NatsController
import com.example.passmanagersvc.web.mapper.proto.pass.CancelPassMapper.failureCancelPassResponse
import com.example.passmanagersvc.web.mapper.proto.pass.CancelPassMapper.successCancelPassResponse
import com.google.protobuf.Parser
import io.nats.client.Connection
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
class CancelPassNatsController(
    override val connection: Connection,
    private val passManagementService: PassManagementService,
) : NatsController<CancelPassRequest, CancelPassResponse> {
    override val subject: String = CANCEL
    override val queueGroup: String = PASS_QUEUE_GROUP
    override val parser: Parser<CancelPassRequest> = CancelPassRequest.parser()

    override fun handle(request: CancelPassRequest): Mono<CancelPassResponse> {
        return passManagementService.cancelPass(request.ownerId, request.id)
            .thenReturn(successCancelPassResponse())
            .onErrorResume {
                failureCancelPassResponse(it).toMono()
            }
    }
}