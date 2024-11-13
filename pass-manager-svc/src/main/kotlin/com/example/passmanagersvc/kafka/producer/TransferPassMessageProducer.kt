package com.example.passmanagersvc.kafka.producer

import com.example.internal.KafkaTopic
import com.example.passmanagersvc.domain.MongoPass
import com.example.passmanagersvc.mapper.proto.pass.TransferredPassMessageMapper.toTransferredPassMessage
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import systems.ajax.kafka.publisher.KafkaPublisher

@Component
class TransferPassMessageProducer(private val kafkaPublisher: KafkaPublisher) {

    fun sendTransferPassMessage(updatedPass: MongoPass, key: String, previousOwnerId: String): Mono<Unit> {
        return kafkaPublisher.publish(
            topic = KafkaTopic.KafkaTransferPassEvents.TRANSFER,
            key = key,
            value = updatedPass.toTransferredPassMessage(previousOwnerId).toByteArray()
        ).thenReturn(Unit)
    }
}
