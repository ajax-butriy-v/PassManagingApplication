package com.example.passmanagersvc.kafka.producer

import com.example.internal.KafkaTopic
import com.example.passmanagersvc.domain.MongoPass
import com.example.passmanagersvc.web.mapper.proto.pass.TransferredPassMessageMapper.toTransferredPassMessage
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import reactor.kotlin.core.publisher.toMono

@Component
class TransferPassMessageProducer(private val kafkaSender: KafkaSender<String, ByteArray>) {

    fun sendTransferPassMessage(updatedPass: MongoPass, key: String, previousOwnerId: String): Mono<Unit> {
        val senderRecord = SenderRecord.create(
            ProducerRecord(
                KafkaTopic.KafkaTransferPassEvents.TRANSFER,
                key,
                updatedPass.toTransferredPassMessage(previousOwnerId).toByteArray()
            ),
            null
        )
        return kafkaSender.send(senderRecord.toMono()).then(Unit.toMono())
    }
}
