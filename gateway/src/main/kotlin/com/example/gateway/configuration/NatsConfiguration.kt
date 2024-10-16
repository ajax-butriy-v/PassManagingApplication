package com.example.gateway.configuration

import io.nats.client.Connection
import io.nats.client.Nats
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class NatsConfiguration(@Value("\${nats.uri}") val natsUri: String) {

    @Bean
    fun natsConnection(): Connection = Nats.connect()
}
