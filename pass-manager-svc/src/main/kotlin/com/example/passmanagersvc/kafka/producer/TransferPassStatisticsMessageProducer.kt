package com.example.passmanagersvc.kafka.producer

import com.example.internal.KafkaTopic
import com.example.internal.input.reqreply.TransferredPassStatisticsMessage
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import systems.ajax.kafka.publisher.KafkaPublisher

@Component
class TransferPassStatisticsMessageProducer(private val kafkaPublisher: KafkaPublisher) {

    fun sendTransferPassStatisticsMessage(message: TransferredPassStatisticsMessage, passTypeId: String): Mono<Unit> {
        return kafkaPublisher.publish(
            topic = KafkaTopic.KafkaTransferPassEvents.TRANSFER_STATISTICS,
            key = passTypeId,
            value = message.toByteArray()
        ).thenReturn(Unit)
    }
}
