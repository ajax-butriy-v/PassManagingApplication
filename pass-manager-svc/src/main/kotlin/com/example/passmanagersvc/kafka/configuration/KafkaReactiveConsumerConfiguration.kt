package com.example.passmanagersvc.kafka.configuration

import com.example.internal.KafkaTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions

@Configuration
class KafkaReactiveConsumerConfiguration(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    private val kafkaProperties: KafkaProperties,
) {

    @Bean
    fun kafkaReceiver(): KafkaReceiver<String, ByteArray> {
        val properties = kafkaProperties.consumer.buildProperties(null).apply {
            putAll(
                mapOf(
                    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                    ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ByteArrayDeserializer::class.java,
                    ConsumerConfig.GROUP_ID_CONFIG to CONSUMER_TRANSFER_PASS_GROUP
                )
            )
        }
        val receiverOptions = ReceiverOptions.create<String, ByteArray>(properties)
            .subscription(setOf(KafkaTopic.KafkaTransferPassEvents.TRANSFER))
        return KafkaReceiver.create(receiverOptions)
    }

    companion object {
        private const val CONSUMER_TRANSFER_PASS_GROUP = "transferPassConsumerGroup"
    }
}
