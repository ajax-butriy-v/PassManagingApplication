package com.example.passmanagersvc.kafka.configuration

import com.example.internal.KafkaTopic
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions

@Configuration
class KafkaReactiveProducerConfiguration(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
) {
    @Bean
    fun reactiveKafkaProducerTemplate(): KafkaSender<String, ByteArray> {
        val properties = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to ByteArraySerializer::class.java
        )
        return KafkaSender.create(SenderOptions.create(properties))
    }

    @Bean
    fun transferPassTopic(): NewTopic {
        return TopicBuilder.name(KafkaTopic.KafkaTransferPassEvents.TRANSFER)
            .partitions(TRANSFER_PASS_TOPICS_PARTITIONS_AMOUNT)
            .build()
    }

    @Bean
    fun transferPassStatisticsTopic(): NewTopic {
        return TopicBuilder.name(KafkaTopic.KafkaTransferPassEvents.TRANSFER_STATISTICS)
            .partitions(TRANSFER_PASS_TOPICS_PARTITIONS_AMOUNT)
            .build()
    }

    companion object {
        const val TRANSFER_PASS_TOPICS_PARTITIONS_AMOUNT = 3
    }
}
