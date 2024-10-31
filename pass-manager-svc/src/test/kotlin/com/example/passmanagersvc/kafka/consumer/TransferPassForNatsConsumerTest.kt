package com.example.passmanagersvc.kafka.consumer

import com.example.commonmodel.Pass
import com.example.internal.NatsSubject
import com.example.internal.input.reqreply.TransferredPassMessage
import com.example.passmanagersvc.kafka.producer.TransferPassMessageProducer
import com.example.passmanagersvc.service.PassTypeService
import com.example.passmanagersvc.util.PassFixture.passFromDb
import com.example.passmanagersvc.util.PassFixture.passTypeToCreate
import io.nats.client.Dispatcher
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.test.test
import java.time.Duration


@SpringBootTest
@ActiveProfiles("test")
internal class TransferPassForNatsConsumerTest {
    @Autowired
    private lateinit var transferPassMessageProducer: TransferPassMessageProducer

    @Autowired
    private lateinit var transferPassForNatsKafkaReceiver: TransferPassForNatsConsumer

    @Autowired
    private lateinit var dispatcher: Dispatcher

    @Autowired
    private lateinit var reactiveMongoTemplate: ReactiveMongoTemplate

    @Autowired
    private lateinit var passTypeService: PassTypeService

    @Test
    fun `nats kafka receiver should publish to NATS subject according to pass type name`() {
        // GIVEN
        val passType = reactiveMongoTemplate.insert(passTypeToCreate).block()!!
        val updatedPass = reactiveMongoTemplate.insert(passFromDb.copy(passTypeId = passType.id)).block()!!
        val passTypeName = passType.name.orEmpty()
        val key = ObjectId.get().toString()
        val previousOwnerId = ObjectId.get().toString()

        val receivedMessages = mutableListOf<Pass>()
        transferPassForNatsKafkaReceiver.consumeTransferMessageAndPublishToNats()
        subscribeToPassesByType(passTypeName)
            .doOnNext { receivedMessages.add(it) }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()


        // WHEN
        transferPassMessageProducer.sendTransferPassMessage(updatedPass, key, previousOwnerId)
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()

        // THEN
        await().atMost(Duration.ofSeconds(15)).untilAsserted {
            assertThat(receivedMessages).isNotEmpty
        }

        receivedMessages.map { it.passTypeId }.toFlux()
            .flatMap { passTypeId -> passTypeService.getById(passTypeId) }
            .test()
            .assertNext {
                assertThat(it.name).isEqualTo(passTypeName)
            }
            .verifyComplete()
    }

    private fun subscribeToPassesByType(passTypeName: String): Flux<Pass> {
        val subjectName = NatsSubject.Pass.subjectByPassTypeName(passTypeName)
        return Flux.create { fluxSink ->
            val subscription = dispatcher.subscribe(subjectName) { message ->
                val transferredMessage = TransferredPassMessage.parseFrom(message.data)
                fluxSink.next(transferredMessage)
            }
            fluxSink.onDispose { dispatcher.unsubscribe(subscription) }
        }.map(TransferredPassMessage::getPass)
    }
}
