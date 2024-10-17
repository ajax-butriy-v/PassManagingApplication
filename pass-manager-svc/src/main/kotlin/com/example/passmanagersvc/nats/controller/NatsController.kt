package com.example.passmanagersvc.nats.controller

import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.Parser
import io.nats.client.Connection
import reactor.core.publisher.Mono

interface NatsController<T : GeneratedMessageV3, R : GeneratedMessageV3> {
    val connection: Connection
    val subject: String
    val queueGroup: String
    val parser: Parser<T>
    val responseClassType: Class<R>
    fun handle(request: T): Mono<R>
}
