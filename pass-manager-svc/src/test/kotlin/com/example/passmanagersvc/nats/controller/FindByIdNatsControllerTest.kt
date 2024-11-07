package com.example.passmanagersvc.nats.controller

import com.example.internal.NatsSubject.Pass.FIND_BY_ID
import com.example.internal.input.reqreply.FindPassByIdResponse
import com.example.passmanagersvc.mapper.proto.pass.FindPassByIdMapper.toProto
import com.example.passmanagersvc.mapper.proto.pass.FindPassByIdMapper.toSuccessFindPassByIdResponse
import com.example.passmanagersvc.repositories.impl.MongoPassRepository
import com.example.passmanagersvc.util.IntegrationTest
import com.example.passmanagersvc.util.PassFixture
import com.example.passmanagersvc.util.PassProtoFixture.failureFindPassByIdResponseWithPassNotFound
import com.example.passmanagersvc.util.PassProtoFixture.findPassByIdRequest
import io.nats.client.Connection
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import kotlin.test.Test

internal class FindByIdNatsControllerTest : IntegrationTest() {
    @Autowired
    private lateinit var connection: Connection

    @Autowired
    // @Qualifier("redisPassRepository")
    private lateinit var passRepository: MongoPassRepository

    @Test
    fun `find by id should return corresponding valid proto response`() {
        // GIVEN
        val pass = passRepository.insert(PassFixture.passToCreate).block()!!
        val findByIdRequest = findPassByIdRequest(pass.id.toString())
        val expectedResponse = pass.toProto().toSuccessFindPassByIdResponse()

        // WHEN
        val message = connection.requestWithTimeout(FIND_BY_ID, findByIdRequest.toByteArray(), Duration.ofSeconds(10))

        // THEN
        val actualResponse = FindPassByIdResponse.parser().parseFrom(message.get().data)

        assertThat(actualResponse).isEqualTo(expectedResponse)
    }

    @Test
    fun `find by invalid id should result in not found response`() {
        // GIVEN
        val invalidId = ObjectId.get().toString()
        val invalidFindByIdRequest = findPassByIdRequest(invalidId)
        val expectedResponse = failureFindPassByIdResponseWithPassNotFound(invalidId)

        // WHEN
        val message =
            connection.requestWithTimeout(FIND_BY_ID, invalidFindByIdRequest.toByteArray(), Duration.ofSeconds(10))

        // THEN
        val actualResponse = FindPassByIdResponse.parser().parseFrom(message.get().data)
        assertThat(actualResponse).isEqualTo(expectedResponse)
    }
}
