package com.example.passmanagersvc.nats.handler

import com.example.internal.NatsSubject.Pass.CREATE
import com.example.internal.input.reqreply.CreatePassResponse
import com.example.passmanagersvc.application.port.out.PassOwnerRepositoryOutPort
import com.example.passmanagersvc.application.port.out.PassTypeRepositoryOutPort
import com.example.passmanagersvc.infrastructure.kafka.mapper.TransferredPassMessageMapper.toProto
import com.example.passmanagersvc.infrastructure.mongo.mapper.PassOwnerMapper.toDomain
import com.example.passmanagersvc.infrastructure.nats.mapper.CreatePassMapper.toSuccessCreatePassResponse
import com.example.passmanagersvc.util.IntegrationTest
import com.example.passmanagersvc.util.PassFixture.passToCreate
import com.example.passmanagersvc.util.PassFixture.passTypeToCreate
import com.example.passmanagersvc.util.PassOwnerFixture.getOwnerWithUniqueFields
import com.example.passmanagersvc.util.PassProtoFixture.createPassRequest
import com.example.passmanagersvc.util.PassProtoFixture.failureCreatePassResponseWithPassOwnerNotFound
import com.example.passmanagersvc.util.PassProtoFixture.failureCreatePassResponseWithPassTypeNotFound
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import reactor.kotlin.test.test
import systems.ajax.nats.publisher.api.NatsMessagePublisher
import kotlin.test.Test

internal class CreatePassNatsControllerTest : IntegrationTest() {
    @Autowired
    private lateinit var natsMessagePublisher: NatsMessagePublisher

    @Autowired
    private lateinit var passOwnerRepository: PassOwnerRepositoryOutPort

    @Autowired
    private lateinit var passTypeRepository: PassTypeRepositoryOutPort

    @Test
    fun `creating pass should return corresponding valid proto response`() {
        // GIVEN
        val passOwner = passOwnerRepository.insert(getOwnerWithUniqueFields().toDomain()).block()!!
        val passType = passTypeRepository.insert(passTypeToCreate).block()!!
        val pass = passToCreate.copy(passTypeId = passType.id.toString(), passOwnerId = passOwner.id.toString())
        val expectedResponse = pass.toProto().toSuccessCreatePassResponse()
        val createRequest = createPassRequest(pass)

        // WHEN
        val createdPassMessage = natsMessagePublisher.request(
            CREATE,
            createRequest,
            CreatePassResponse.parser()
        )

        // THEN
        createdPassMessage.test()
            .expectNext(expectedResponse)
            .verifyComplete()
    }

    @Test
    fun `creating with non-existent pass type should return not found response`() {
        // GIVEN
        val passOwner = passOwnerRepository.insert(getOwnerWithUniqueFields().toDomain()).block()!!
        val invalidPassTypeId = ObjectId.get().toString()
        val expectedResponse = failureCreatePassResponseWithPassTypeNotFound(invalidPassTypeId)
        val passToCreate = passToCreate.copy(passOwnerId = passOwner.id.toString(), passTypeId = invalidPassTypeId)
        val invalidCreateRequest = createPassRequest(passToCreate)

        // WHEN
        val createdPassMessage = natsMessagePublisher.request(
            CREATE,
            invalidCreateRequest,
            CreatePassResponse.parser()
        )

        // THEN
        createdPassMessage.test()
            .expectNext(expectedResponse)
            .verifyComplete()
    }

    @Test
    fun `creating with non-existent pass owner should return not found response`() {
        // GIVEN
        val invalidPassOwnerId = ObjectId.get().toString()
        val passType = passTypeRepository.insert(passTypeToCreate).block()!!
        val invalidCreateRequest = createPassRequest(
            passToCreate.copy(passTypeId = passType.id.toString(), passOwnerId = invalidPassOwnerId)
        )
        val expectedResponse = failureCreatePassResponseWithPassOwnerNotFound(invalidPassOwnerId)

        // WHEN
        val createdPassMessage = natsMessagePublisher.request(
            CREATE,
            invalidCreateRequest,
            CreatePassResponse.parser()
        )

        // THEN
        createdPassMessage.test()
            .expectNext(expectedResponse)
            .verifyComplete()
    }
}
