package com.example.passmanagersvc.nats.controller

import com.example.internal.NatsSubject.Pass.CREATE
import com.example.internal.input.reqreply.CreatePassResponse
import com.example.passmanagersvc.repositories.PassOwnerRepository
import com.example.passmanagersvc.repositories.PassTypeRepository
import com.example.passmanagersvc.util.IntegrationTest
import com.example.passmanagersvc.util.PassFixture.passToCreate
import com.example.passmanagersvc.util.PassFixture.passTypeToCreate
import com.example.passmanagersvc.util.PassOwnerFixture.getOwnerWithUniqueFields
import com.example.passmanagersvc.util.PassProtoFixture
import com.example.passmanagersvc.util.PassProtoFixture.createPassRequest
import com.example.passmanagersvc.web.mapper.proto.pass.CreatePassMapper.toSuccessCreatePassResponse
import com.example.passmanagersvc.web.mapper.proto.pass.FindPassByIdMapper.toProto
import io.nats.client.Connection
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import kotlin.test.Test

internal class CreatePassNatsControllerTest : IntegrationTest() {
    @Autowired
    private lateinit var connection: Connection

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
        val createdPassMessage = connection.requestWithTimeout(
            CREATE,
            createRequest.toByteArray(),
            Duration.ofSeconds(5)
        )

        // THEN
        val actualResponse = CreatePassResponse.parser().parseFrom(createdPassMessage.get().data)
        assertThat(actualResponse).isEqualTo(expectedResponse)
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
        val createdPassMessage = connection.requestWithTimeout(
            CREATE,
            invalidCreateRequest.toByteArray(),
            Duration.ofSeconds(5)
        )

        // THEN
        val actualResponse = CreatePassResponse.parser().parseFrom(createdPassMessage.get().data)
        assertThat(actualResponse).isEqualTo(expectedResponse)
    }

    @Test
    fun `creating with non-existent pass owner should return not found response`() {
        // GIVEN
        val invalidPassOwnerId = ObjectId.get()
        val passType = passTypeRepository.insert(passTypeToCreate).block()!!
        val invalidCreateRequest =
            createPassRequest(passToCreate.copy(passTypeId = passType.id, passOwnerId = invalidPassOwnerId))
        val expectedResponse =
            PassProtoFixture.failureCreatePassResponseWithPassOwnerNotFound(invalidPassOwnerId.toString())

        // WHEN
        val createdPassMessage = connection.requestWithTimeout(
            CREATE,
            invalidCreateRequest.toByteArray(),
            Duration.ofSeconds(5)
        )

        // THEN
        val actualResponse = CreatePassResponse.parser().parseFrom(createdPassMessage.get().data)
        assertThat(actualResponse).isEqualTo(expectedResponse)
    }
}
