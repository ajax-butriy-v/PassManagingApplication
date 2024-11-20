package com.example.passmanagersvc.pass.infrastructure.kafka.consumer

import com.example.internal.NatsSubject
import com.example.internal.input.reqreply.TransferredPassMessage
import com.example.passmanagersvc.pass.infrastructure.kafka.producer.TransferPassMessageProducer
import com.example.passmanagersvc.pass.infrastructure.mongo.mapper.PassMapper.toDomain
import com.example.passmanagersvc.pass.util.IntegrationTest
import com.example.passmanagersvc.passtype.application.service.PassTypeService
import com.example.passmanagersvc.passtype.infrastructure.mongo.mapper.PassTypeMapper.toDomain
import com.example.passmanagersvc.util.PassFixture.mongoPassToCreate
import com.example.passmanagersvc.util.PassFixture.mongoPassTypeToCreate
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.test.test
import systems.ajax.nats.mock.junit5.NatsMockExtension
import java.time.Duration

internal class TransferPassForNatsConsumerTest : IntegrationTest() {

    @Autowired
    private lateinit var transferPassMessageProducer: TransferPassMessageProducer

    @Autowired
    private lateinit var reactiveMongoTemplate: ReactiveMongoTemplate

    @Autowired
    private lateinit var passTypeService: PassTypeService

    @Test
    fun `nats kafka receiver should publish to NATS subject according to pass type name`() {
        // GIVEN
        val passType = reactiveMongoTemplate.insert(mongoPassTypeToCreate.copy(name = "test")).block()!!
        val updatedPass = reactiveMongoTemplate.insert(mongoPassToCreate.copy(passTypeId = passType.id)).block()!!
        val passTypeName = passType.name.orEmpty()
        val key = ObjectId.get().toString()
        val previousOwnerId = ObjectId.get().toString()

        transferPassMessageProducer.sendTransferPassMessage(updatedPass.toDomain(), key, previousOwnerId)
            .delaySubscription(Duration.ofSeconds(1))
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()

        // WHEN
        val subject = NatsSubject.Pass.subjectByPassTypeName(passTypeName)
        val capture = natsMockExtension.subscribe(subject, TransferredPassMessage.parser()).capture()

        // THEN
        await().atMost(Duration.ofSeconds(15)).untilAsserted {
            assertThat(capture.getCapturedMessages()).isNotEmpty
        }

        val receivedMessages = capture.getCapturedMessages().map { it.pass }
        receivedMessages.map { it.passTypeId }.toFlux()
            .flatMap { passTypeId -> passTypeService.getById(passTypeId) }
            .test()
            .expectNext(passType.toDomain())
            .verifyComplete()
    }

    companion object {
        @JvmField
        @RegisterExtension
        val natsMockExtension: NatsMockExtension = NatsMockExtension()
    }
}
