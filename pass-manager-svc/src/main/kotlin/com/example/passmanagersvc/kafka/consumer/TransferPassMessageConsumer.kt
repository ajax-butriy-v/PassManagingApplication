package com.example.passmanagersvc.kafka.consumer

import com.example.internal.input.reqreply.TransferredPassMessage
import com.example.passmanagersvc.service.PassOwnerStatisticsService
import com.example.passmanagersvc.web.mapper.proto.pass.CreatePassMapper.toModel
import org.bson.types.ObjectId
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kafka.receiver.KafkaReceiver
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
                receiverRecords.flatMap { record ->
                    record.toMono()
                        .map { TransferredPassMessage.parseFrom(it.value()) }
                        .flatMap(::delegateToStatisticsService)
                        .doFinally { record.receiverOffset().acknowledge() }
                }
            }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
    }

    private fun delegateToStatisticsService(message: TransferredPassMessage): Mono<Unit> {
        val mongoPass = message.pass.toModel().copy(id = ObjectId(message.passId))
        return passOwnerStatisticsService.publishTransferPassStatistics(
            mongoPass,
            message.previousPassOwnerId
        )
    }
}
