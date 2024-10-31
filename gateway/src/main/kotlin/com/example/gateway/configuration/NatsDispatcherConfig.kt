package com.example.gateway.configuration

import io.nats.client.Connection
import io.nats.client.Dispatcher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class NatsDispatcherConfig {
    @Bean
    fun dispatcher(connection: Connection): Dispatcher = connection.createDispatcher()
}
