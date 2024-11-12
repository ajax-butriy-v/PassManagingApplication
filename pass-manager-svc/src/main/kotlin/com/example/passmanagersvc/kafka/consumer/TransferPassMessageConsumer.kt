package com.example.passmanagersvc.kafka.consumer

import com.example.internal.KafkaTopic
import com.example.internal.input.reqreply.TransferredPassMessage
import com.example.passmanagersvc.mapper.proto.pass.CreatePassMapper.toModel
import com.example.passmanagersvc.service.PassOwnerStatisticsService
import com.google.protobuf.Parser
import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import systems.ajax.kafka.handler.KafkaEvent
import systems.ajax.kafka.handler.KafkaHandler
import systems.ajax.kafka.handler.subscription.topic.TopicSingle

@Component
class TransferPassMessageConsumer(
    private val passOwnerStatisticsService: PassOwnerStatisticsService,
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
        val mongoPass = message.pass.toModel().copy(id = ObjectId(message.passId))
        return passOwnerStatisticsService.publishTransferPassStatistics(
            mongoPass,
            message.previousPassOwnerId
        )
    }

    companion object {
        private const val CONSUMER_TRANSFER_PASS_GROUP = "transferPassConsumerGroup"
    }
}
