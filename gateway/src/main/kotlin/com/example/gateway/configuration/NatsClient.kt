package com.example.gateway.configuration

import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.Parser
import io.nats.client.Connection
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class NatsClient(private val natsConnection: Connection) {

    fun <T : GeneratedMessageV3, R : GeneratedMessageV3> request(
        subject: String,
        payload: T,
        parser: Parser<R>,
    ): Mono<R> {
        return Mono.fromFuture { natsConnection.request(subject, payload.toByteArray()) }
            .map { response -> parser.parseFrom(response.data) }
    }
}
