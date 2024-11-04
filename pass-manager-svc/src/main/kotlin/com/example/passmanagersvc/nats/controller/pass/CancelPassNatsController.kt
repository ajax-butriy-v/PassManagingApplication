package com.example.passmanagersvc.nats.controller.pass

import com.example.internal.NatsSubject.Pass.CANCEL
import com.example.internal.input.reqreply.CancelPassRequest
import com.example.internal.input.reqreply.CancelPassResponse
import com.example.passmanagersvc.mapper.proto.pass.CancelPassMapper.failureCancelPassResponse
import com.example.passmanagersvc.mapper.proto.pass.CancelPassMapper.successCancelPassResponse
import com.example.passmanagersvc.nats.controller.NatsController
import com.example.passmanagersvc.service.PassManagementService
import com.google.protobuf.Parser
import io.nats.client.Connection
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
    override val responseClass = CancelPassResponse.getDefaultInstance()

    override fun handle(request: CancelPassRequest): Mono<CancelPassResponse> {
        return passManagementService.cancelPass(request.ownerId, request.id)
            .thenReturn(successCancelPassResponse())
            .onErrorResume {
                logger.error("Error occurred while executing", it)
                failureCancelPassResponse(it).toMono()
            }
    }

    companion object {
        const val PASS_QUEUE_GROUP = "passQueueGroup"
        private val logger: Logger = LoggerFactory.getLogger(CancelPassNatsController::class.java)
    }
}
