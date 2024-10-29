package com.example.passmanagersvc.kafka.consumer

import com.example.internal.KafkaTopic
import com.example.internal.input.reqreply.TransferredPassStatisticsMessage
import com.example.passmanagersvc.kafka.producer.TransferPassMessageProducer
import com.example.passmanagersvc.util.PassFixture.passFromDb
import com.example.passmanagersvc.util.PassFixture.passTypeToCreate
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.test.context.ActiveProfiles
import reactor.core.scheduler.Schedulers
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import java.time.Duration

@SpringBootTest
@Import(TransferPassMessageConsumerTest.ConsumerForSecondTopic::class)
@ActiveProfiles("test")
class TransferPassMessageConsumerTest {
    @Autowired
    private lateinit var transferPassMessageProducer: TransferPassMessageProducer

    @Autowired
    private lateinit var reactiveMongoTemplate: ReactiveMongoTemplate

    @Autowired
    private lateinit var consumerForTransferStatistics: KafkaReceiver<String, ByteArray>

    @Test
    fun `transfer pass consumer, which is listening to transfer topic, should produce to transfer stats topic `() {
        // GIVEN
        val passType = reactiveMongoTemplate.insert(passTypeToCreate).block()!!
        val updatedPass = reactiveMongoTemplate.insert(passFromDb.copy(passTypeId = passType.id)).block()!!

        val key = ObjectId.get().toString()
        val previousOwnerId = ObjectId.get().toString()

        val receivedMessages = mutableListOf<TransferredPassStatisticsMessage>()
        consumerForTransferStatistics.receive()
            .map { TransferredPassStatisticsMessage.parseFrom(it.value()) }
            .doOnNext { receivedMessages.add(it) }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()

        // WHEN
        transferPassMessageProducer.sendTransferPassMessage(updatedPass, key, previousOwnerId)
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()

        // THEN
        await().atMost(Duration.ofSeconds(15)).untilAsserted {
            assertThat(receivedMessages).isNotEmpty
            assertThat(receivedMessages.map { it.passId }).contains(updatedPass.id.toString())
        }
    }

    class ConsumerForSecondTopic(@Value("\${spring.kafka.bootstrap-servers}") val bootstrapServers: String) {
        @Bean
        fun consumerForTransferStatistics(kafkaProperties: KafkaProperties): KafkaReceiver<String, ByteArray>? {
            val properties = kafkaProperties.consumer.buildProperties(null).apply {
                putAll(
                    mapOf(
                        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ByteArrayDeserializer::class.java,
                        ConsumerConfig.GROUP_ID_CONFIG to CONSUMER_TRANSFER_STATISTICS_PASS_GROUP
                    )
                )
            }
            val receiverOptions = ReceiverOptions.create<String, ByteArray>(properties)
                .subscription(setOf(KafkaTopic.KafkaTransferPassEvents.TRANSFER_STATISTICS))
            return KafkaReceiver.create(receiverOptions)
        }

        companion object {
            private const val CONSUMER_TRANSFER_STATISTICS_PASS_GROUP = "transferStatsPassConsumerGroup"
        }
    }
}
