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
    fun transferPassKafkaReceiver(): KafkaReceiver<String, ByteArray> {
        return KafkaReceiver.create(
            createConsumerProperties(
                consumerGroup = CONSUMER_TRANSFER_PASS_GROUP,
                topic = KafkaTopic.KafkaTransferPassEvents.TRANSFER
            )
        )
    }

    @Bean
    fun transferPassForNatsKafkaReceiver(): KafkaReceiver<String, ByteArray> {
        return KafkaReceiver.create(
            createConsumerProperties(
                consumerGroup = CONSUMER_TRANSFER_PASS_FOR_NATS_GROUP,
                topic = KafkaTopic.KafkaTransferPassEvents.TRANSFER
            )
        )
    }

    private fun createConsumerProperties(consumerGroup: String, topic: String): ReceiverOptions<String, ByteArray> {
        val properties = kafkaProperties.consumer.buildProperties(null).apply {
            putAll(
                mapOf(
                    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                    ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ByteArrayDeserializer::class.java,
                    ConsumerConfig.GROUP_ID_CONFIG to consumerGroup
                )
            )
        }
        return ReceiverOptions.create<String, ByteArray>(properties).subscription(setOf(topic))
    }

    companion object {
        private const val CONSUMER_TRANSFER_PASS_GROUP = "transferPassConsumerGroup"
        private const val CONSUMER_TRANSFER_PASS_FOR_NATS_GROUP = "transferPassForNatsConsumerGroup"
    }
}
