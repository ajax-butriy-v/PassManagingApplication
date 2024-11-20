package com.example.passmanagersvc.pass.infrastructure.kafka.producer

import com.example.internal.KafkaTopic
import com.example.internal.input.reqreply.TransferredPassStatisticsMessage
import com.example.passmanagersvc.pass.application.port.output.TransferPassStatisticsMessageProducerOutPort
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import systems.ajax.kafka.publisher.KafkaPublisher

@Component
class TransferPassStatisticsMessageProducer(private val kafkaPublisher: KafkaPublisher) :
    TransferPassStatisticsMessageProducerOutPort {

    override fun sendTransferPassStatisticsMessage(
        message: TransferredPassStatisticsMessage,
        passTypeId: String,
    ): Mono<Unit> {
        return kafkaPublisher.publish(
            topic = KafkaTopic.KafkaTransferPassEvents.TRANSFER_STATISTICS,
            key = passTypeId,
            value = message.toByteArray()
        ).thenReturn(Unit)
    }
}
