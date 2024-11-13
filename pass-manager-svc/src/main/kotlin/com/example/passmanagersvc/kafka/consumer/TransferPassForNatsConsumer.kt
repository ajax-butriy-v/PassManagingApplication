package com.example.passmanagersvc.kafka.consumer

import com.example.internal.KafkaTopic
import com.example.internal.NatsSubject
import com.example.internal.input.reqreply.TransferredPassMessage
import com.example.passmanagersvc.service.PassTypeService
import com.google.protobuf.Parser
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import systems.ajax.kafka.handler.KafkaEvent
import systems.ajax.kafka.handler.KafkaHandler
import systems.ajax.kafka.handler.subscription.topic.TopicSingle
import systems.ajax.nats.publisher.api.NatsMessagePublisher

@Component
class TransferPassForNatsConsumer(
    private val passTypeService: PassTypeService,
    private val natsMessagePublisher: NatsMessagePublisher,
) : KafkaHandler<TransferredPassMessage, TopicSingle> {

    override val groupId: String = CONSUMER_TRANSFER_PASS_FOR_NATS_GROUP
    override val parser: Parser<TransferredPassMessage> = TransferredPassMessage.parser()
    override val subscriptionTopics: TopicSingle = TopicSingle(KafkaTopic.KafkaTransferPassEvents.TRANSFER)

    override fun handle(kafkaEvent: KafkaEvent<TransferredPassMessage>): Mono<Unit> {
        return publishToNatsSubject(kafkaEvent.data)
            .doOnSuccess { kafkaEvent.ack() }
            .thenReturn(Unit)
    }

    private fun publishToNatsSubject(message: TransferredPassMessage): Mono<Unit> {
        return passTypeService.getById(message.pass.passTypeId)
            .map { passType -> passType.name.orEmpty() }
            .flatMap { passTypeName ->
                natsMessagePublisher.publish(
                    NatsSubject.Pass.subjectByPassTypeName(passTypeName),
                    message
                ).toMono()
            }
    }

    companion object {
        private const val CONSUMER_TRANSFER_PASS_FOR_NATS_GROUP = "transferPassForNatsConsumerGroup"
    }
}
