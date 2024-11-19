package com.example.gateway.infrastructure.nats

import com.example.gateway.application.port.output.NatsHandlerPassMessageInPort
import com.example.internal.NatsSubject
import com.example.internal.NatsSubject.Pass.CREATE
import com.example.internal.NatsSubject.Pass.FIND_BY_ID
import com.example.internal.input.reqreply.CreatePassRequest
import com.example.internal.input.reqreply.CreatePassResponse
import com.example.internal.input.reqreply.FindPassByIdRequest
import com.example.internal.input.reqreply.FindPassByIdResponse
import com.example.internal.input.reqreply.TransferredPassMessage
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import systems.ajax.nats.handler.api.NatsHandlerManager
import systems.ajax.nats.publisher.api.NatsMessagePublisher

@Component
class NatsHandlerPassMessageHandler(
    private val natsMessagePublisher: NatsMessagePublisher,
    private val natsHandlerManager: NatsHandlerManager,
) : NatsHandlerPassMessageInPort {
    override fun getAllTransferredPasses(passTypeName: String): Flux<TransferredPassMessage> {
        return natsHandlerManager
            .subscribe(
                subject = NatsSubject.Pass.subjectByPassTypeName(passTypeName),
                deserializer = { message -> TransferredPassMessage.parseFrom(message.data) }
            )
    }

    override fun findPassById(request: FindPassByIdRequest): Mono<FindPassByIdResponse> {
        return natsMessagePublisher.request(FIND_BY_ID, request, FindPassByIdResponse.parser())
    }

    override fun createPass(request: CreatePassRequest): Mono<CreatePassResponse> {
        return natsMessagePublisher.request(CREATE, request, CreatePassResponse.parser())
    }
}
