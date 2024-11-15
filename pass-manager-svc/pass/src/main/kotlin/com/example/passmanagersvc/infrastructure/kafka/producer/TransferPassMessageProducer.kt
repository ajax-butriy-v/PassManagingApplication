package com.example.passmanagersvc.infrastructure.kafka.producer

import com.example.internal.KafkaTopic
import com.example.passmanagersvc.application.port.output.TransferPassMessageProducerOutPort
import com.example.passmanagersvc.domain.Pass
import com.example.passmanagersvc.infrastructure.kafka.mapper.TransferredPassMessageMapper.toTransferredPassMessage
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import systems.ajax.kafka.publisher.KafkaPublisher

@Component
class TransferPassMessageProducer(private val kafkaPublisher: KafkaPublisher) : TransferPassMessageProducerOutPort {
    override fun sendTransferPassMessage(updatedPass: Pass, key: String, previousOwnerId: String): Mono<Unit> {
        return kafkaPublisher.publish(
            topic = KafkaTopic.KafkaTransferPassEvents.TRANSFER,
            key = key,
            value = updatedPass.toTransferredPassMessage(previousOwnerId).toByteArray()
        ).thenReturn(Unit)
    }
}
