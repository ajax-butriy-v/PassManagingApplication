package com.example.passmanagersvc.nats.controller

import com.example.internal.NatsSubject.Pass.CREATE
import com.example.internal.input.reqreply.CreatePassResponse
import com.example.passmanagersvc.mapper.proto.pass.CreatePassMapper.toSuccessCreatePassResponse
import com.example.passmanagersvc.mapper.proto.pass.FindPassByIdMapper.toProto
import com.example.passmanagersvc.repositories.PassOwnerRepository
import com.example.passmanagersvc.repositories.PassTypeRepository
import com.example.passmanagersvc.util.IntegrationTest
import com.example.passmanagersvc.util.PassFixture.passToCreate
import com.example.passmanagersvc.util.PassFixture.passTypeToCreate
import com.example.passmanagersvc.util.PassOwnerFixture.getOwnerWithUniqueFields
import com.example.passmanagersvc.util.PassProtoFixture
import com.example.passmanagersvc.util.PassProtoFixture.createPassRequest
import com.example.passmanagersvc.util.PassProtoFixture.failureCreatePassResponseWithPassOwnerNotFound
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import reactor.kotlin.test.test
import systems.ajax.nats.publisher.api.NatsMessagePublisher
import kotlin.test.Test

internal class CreatePassNatsControllerTest : IntegrationTest() {
    @Autowired
    private lateinit var natsMessagePublisher: NatsMessagePublisher

    @Autowired
    private lateinit var passOwnerRepository: PassOwnerRepository

    @Autowired
    private lateinit var passTypeRepository: PassTypeRepository

    @Test
    fun `creating pass should return corresponding valid proto response`() {
        // GIVEN
        val passOwner = passOwnerRepository.insert(getOwnerWithUniqueFields()).block()
        val passType = passTypeRepository.insert(passTypeToCreate).block()
        val pass = passToCreate.copy(passTypeId = passType!!.id, passOwnerId = passOwner!!.id)
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
        val passOwner = passOwnerRepository.insert(getOwnerWithUniqueFields()).block()!!
        val invalidPassTypeId = ObjectId.get()
        val expectedResponse = PassProtoFixture.failureCreatePassResponseWithPassTypeNotFound(invalidPassTypeId)
        val passToCreate = passToCreate.copy(passOwnerId = passOwner.id, passTypeId = invalidPassTypeId)
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
        val invalidPassOwnerId = ObjectId.get()
        val passType = passTypeRepository.insert(passTypeToCreate).block()!!
        val invalidCreateRequest = createPassRequest(
            passToCreate.copy(passTypeId = passType.id, passOwnerId = invalidPassOwnerId)
        )
        val expectedResponse = failureCreatePassResponseWithPassOwnerNotFound(invalidPassOwnerId.toString())

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
