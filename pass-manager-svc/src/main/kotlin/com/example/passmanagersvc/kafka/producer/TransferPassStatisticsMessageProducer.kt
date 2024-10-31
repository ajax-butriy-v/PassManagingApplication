package com.example.passmanagersvc.kafka.producer

import com.example.internal.KafkaTopic
import com.example.internal.input.reqreply.TransferredPassStatisticsMessage
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import reactor.kotlin.core.publisher.toMono

@Component
class TransferPassStatisticsMessageProducer(private val kafkaSender: KafkaSender<String, ByteArray>) {

    fun sendTransferPassStatisticsMessage(message: TransferredPassStatisticsMessage, passTypeId: String): Mono<Unit> {
        val senderRecord = SenderRecord.create(
            ProducerRecord(
                KafkaTopic.KafkaTransferPassEvents.TRANSFER_STATISTICS,
                passTypeId,
                message.toByteArray()
            ),
            null
        )
        return kafkaSender.send(senderRecord.toMono()).then(Unit.toMono())
    }
}
