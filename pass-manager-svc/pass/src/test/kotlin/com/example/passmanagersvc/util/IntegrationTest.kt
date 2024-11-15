package com.example.passmanagersvc.util

import org.apache.kafka.clients.admin.Admin
import org.apache.kafka.clients.admin.KafkaAdminClient
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@Import(IntegrationTest.KafkaTestConfiguration::class)
abstract class IntegrationTest {
    class KafkaTestConfiguration {
        @Bean
        fun adminClient(kafkaAdmin: KafkaAdmin): Admin {
            return KafkaAdminClient.create(kafkaAdmin.configurationProperties)
        }

        @Bean
        fun consumerFactory(kafkaProperties: KafkaProperties): ConsumerFactory<String, ByteArray> {
            return DefaultKafkaConsumerFactory(
                kafkaProperties.buildConsumerProperties(null),
                StringDeserializer(),
                ByteArrayDeserializer()
            )
        }
    }
}
