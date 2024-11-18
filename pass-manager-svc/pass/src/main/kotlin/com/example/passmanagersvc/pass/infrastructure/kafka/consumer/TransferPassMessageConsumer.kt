package com.example.passmanagersvc.pass.infrastructure.kafka.consumer

import com.example.internal.KafkaTopic
import com.example.internal.input.reqreply.TransferredPassMessage
import com.example.passmanagersvc.pass.application.port.input.PassManagementServiceInputPort
import com.example.passmanagersvc.pass.infrastructure.nats.mapper.ProtoPassMapper.toDomain
import com.google.protobuf.Parser
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import systems.ajax.kafka.handler.KafkaEvent
import systems.ajax.kafka.handler.KafkaHandler
import systems.ajax.kafka.handler.subscription.topic.TopicSingle

@Component
class TransferPassMessageConsumer(
    private val passManagementServiceInputPort: PassManagementServiceInputPort,
) : KafkaHandler<TransferredPassMessage, TopicSingle> {

    override val groupId: String = CONSUMER_TRANSFER_PASS_GROUP
    override val parser: Parser<TransferredPassMessage> = TransferredPassMessage.parser()
    override val subscriptionTopics: TopicSingle = TopicSingle(KafkaTopic.KafkaTransferPassEvents.TRANSFER)

    override fun handle(kafkaEvent: KafkaEvent<TransferredPassMessage>): Mono<Unit> {
        return kafkaEvent.toMono()
            .flatMap { event ->
                delegateToStatisticsService(event.data)
                    .doOnSuccess { event.ack() }
            }
            .thenReturn(Unit)
    }

    private fun delegateToStatisticsService(message: TransferredPassMessage): Mono<Unit> {
        val pass = message.pass.toDomain().copy(id = message.passId)
        return passManagementServiceInputPort.publishTransferPassStatistics(
            pass,
            message.previousPassOwnerId
        )
    }

    companion object {
        private const val CONSUMER_TRANSFER_PASS_GROUP = "transferPassConsumerGroup"
    }
}
