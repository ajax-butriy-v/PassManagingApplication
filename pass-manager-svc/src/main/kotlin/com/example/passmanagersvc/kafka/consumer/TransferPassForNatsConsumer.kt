package com.example.passmanagersvc.kafka.consumer

import com.example.internal.NatsSubject
import com.example.internal.input.reqreply.TransferredPassMessage
import com.example.passmanagersvc.service.PassTypeService
import io.nats.client.Connection
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kafka.receiver.KafkaReceiver
import reactor.kotlin.core.publisher.toMono

@Component
class TransferPassForNatsConsumer(
    private val transferPassForNatsKafkaReceiver: KafkaReceiver<String, ByteArray>,
    private val passTypeService: PassTypeService,
    private val natsConnection: Connection,
) {
    @EventListener(ApplicationReadyEvent::class)
    fun consumeTransferMessageAndPublishToNats() {
        transferPassForNatsKafkaReceiver.receiveBatch()
            .flatMap { receiverRecords ->
                receiverRecords.flatMap { record ->
                    val transferredPassMessage = TransferredPassMessage.parseFrom(record.value())
                    publishToNatsSubject(transferredPassMessage).doFinally { record.receiverOffset().acknowledge() }
                }
            }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
    }

    private fun publishToNatsSubject(message: TransferredPassMessage): Mono<Unit> {
        return passTypeService.getById(message.pass.passTypeId)
            .map { passType -> passType.name.orEmpty() }
            .flatMap { passTypeName ->
                natsConnection.publish(
                    NatsSubject.Pass.subjectByPassTypeName(passTypeName),
                    message.toByteArray()
                ).toMono()
            }
    }
}
