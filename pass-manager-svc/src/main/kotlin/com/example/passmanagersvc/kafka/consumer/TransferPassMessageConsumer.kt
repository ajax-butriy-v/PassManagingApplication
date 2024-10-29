package com.example.passmanagersvc.kafka.consumer

import com.example.internal.input.reqreply.TransferredPassMessage
import com.example.passmanagersvc.domain.MongoPass
import com.example.passmanagersvc.service.PassOwnerStatisticsService
import com.example.passmanagersvc.web.mapper.proto.pass.CreatePassMapper.toModel
import org.bson.types.ObjectId
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverRecord
import reactor.kotlin.core.publisher.toMono

@Component
class TransferPassMessageConsumer(
    private val kafkaReceiver: KafkaReceiver<String, ByteArray>,
    private val passOwnerStatisticsService: PassOwnerStatisticsService,
) {
    @EventListener(ApplicationReadyEvent::class)
    fun listenToTransferPassMessageTopic() {
        kafkaReceiver.receiveBatch()
            .flatMap { receiverRecords ->
                receiverRecords.flatMap(::delegateToStatisticsService)
            }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
    }

    private fun delegateToStatisticsService(record: ReceiverRecord<String, ByteArray>): Mono<Unit> {
        return recordToPassAndPreviousOwnerIdPair(record)
            .flatMap { (updatedPass, previousOwnerId) ->
                passOwnerStatisticsService.publishTransferPassStatistics(
                    updatedPass,
                    previousOwnerId
                )
            }
            .doFinally { record.receiverOffset().acknowledge() }
    }

    private fun recordToPassAndPreviousOwnerIdPair(
        record: ReceiverRecord<String, ByteArray>,
    ): Mono<Pair<MongoPass, String>> {
        return record.toMono()
            .map { TransferredPassMessage.parseFrom(it.value()) }
            .map { message ->
                val pass = message.pass
                val mongoPass = pass.toModel().copy(id = ObjectId(message.passId))
                mongoPass to message.previousPassOwnerId
            }
    }
}
