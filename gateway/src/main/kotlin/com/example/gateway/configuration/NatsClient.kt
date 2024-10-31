package com.example.gateway.configuration

import com.example.commonmodel.Pass
import com.example.internal.NatsSubject
import com.example.internal.input.reqreply.TransferredPassMessage
import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.Parser
import io.nats.client.Connection
import io.nats.client.Dispatcher
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class NatsClient(private val natsConnection: Connection, private val dispatcher: Dispatcher) {

    fun <T : GeneratedMessageV3, R : GeneratedMessageV3> request(
        subject: String,
        payload: T,
        parser: Parser<R>,
    ): Mono<R> {
        return Mono.fromFuture { natsConnection.request(subject, payload.toByteArray()) }
            .map { response -> parser.parseFrom(response.data) }
    }

    fun subscribeToPassesByType(passTypeName: String): Flux<Pass> {
        val subjectName = NatsSubject.Pass.subjectByPassTypeName(passTypeName)
        return Flux.create { fluxSink ->
            val subscription = dispatcher.subscribe(subjectName) { message ->
                val transferredMessage = TransferredPassMessage.parseFrom(message.data)
                fluxSink.next(transferredMessage)
            }
            fluxSink.onDispose { dispatcher.unsubscribe(subscription) }
        }.map(TransferredPassMessage::getPass)
    }
}
