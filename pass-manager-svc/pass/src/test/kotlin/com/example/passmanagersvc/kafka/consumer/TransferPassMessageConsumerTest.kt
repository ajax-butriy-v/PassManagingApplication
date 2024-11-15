package com.example.passmanagersvc.kafka.consumer

import com.example.internal.KafkaTopic
import com.example.internal.input.reqreply.TransferredPassStatisticsMessage
import com.example.passmanagersvc.infrastructure.kafka.producer.TransferPassMessageProducer
import com.example.passmanagersvc.infrastructure.mongo.mapper.PassMapper.toDomain
import com.example.passmanagersvc.util.IntegrationTest
import com.example.passmanagersvc.util.PassFixture.mongoPassToCreate
import com.example.passmanagersvc.util.PassFixture.mongoPassTypeToCreate
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import reactor.core.scheduler.Schedulers
import systems.ajax.kafka.mock.KafkaMockExtension

internal class TransferPassMessageConsumerTest : IntegrationTest() {
    @Autowired
    private lateinit var transferPassMessageProducer: TransferPassMessageProducer

    @Autowired
    private lateinit var reactiveMongoTemplate: ReactiveMongoTemplate

    @Test
    fun `transfer pass consumer, which is listening to transfer topic, should produce to transfer stats topic `() {
        // GIVEN
        val passType = reactiveMongoTemplate.insert(mongoPassTypeToCreate).block()!!
        val updatedPass = reactiveMongoTemplate.insert(mongoPassToCreate.copy(passTypeId = passType.id)).block()!!

        val key = ObjectId.get().toString()
        val previousOwnerId = ObjectId.get().toString()

        val result = kafkaMockExtension.listen(
            KafkaTopic.KafkaTransferPassEvents.TRANSFER_STATISTICS,
            TransferredPassStatisticsMessage.parser()
        )

        // WHEN
        transferPassMessageProducer.sendTransferPassMessage(updatedPass.toDomain(), key, previousOwnerId)
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()

        val transferredPassStatisticsMessage = result.awaitFirst({
            it.passId == updatedPass.id.toString()
        })
        assertThat(transferredPassStatisticsMessage).isNotNull
    }

    companion object {
        @JvmField
        @RegisterExtension
        val kafkaMockExtension: KafkaMockExtension = KafkaMockExtension()
    }
}
