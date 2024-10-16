package com.example.passmanagersvc.web.controller.nats

import com.example.internal.NatsSubject.Pass.FIND_BY_ID
import com.example.passmanagersvc.input.reqreply.FindPassByIdResponse
import com.example.passmanagersvc.repositories.PassRepository
import com.example.passmanagersvc.util.PassFixture
import com.example.passmanagersvc.util.PassProtoFixture.failureFindPassByIdResponseWithPassNotFound
import com.example.passmanagersvc.util.PassProtoFixture.findPassByIdRequest
import com.example.passmanagersvc.web.mapper.proto.pass.FindPassByIdMapper.toProto
import com.example.passmanagersvc.web.mapper.proto.pass.FindPassByIdMapper.toSuccessFindPassByIdResponse
import io.nats.client.Connection
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.Duration
import kotlin.test.Test

@SpringBootTest
@ActiveProfiles("test")
class FindByIdNatsControllerTest {
    @Autowired
    private lateinit var connection: Connection

    @Autowired
    private lateinit var passRepository: PassRepository

    @Test
    fun `find by id should return corresponding valid proto response`() {
        // GIVEN
        val pass = passRepository.insert(PassFixture.passToCreate).block()
        val findByIdRequest = findPassByIdRequest(pass?.id)

        // WHEN
        val message = connection.requestWithTimeout(FIND_BY_ID, findByIdRequest.toByteArray(), Duration.ofSeconds(10))

        // THEN
        val expectedResponse = pass!!.toProto().toSuccessFindPassByIdResponse()
        val actualResponse = FindPassByIdResponse.parser().parseFrom(message.get().data)
        assertThat(actualResponse).isEqualTo(expectedResponse)
    }

    @Test
    fun `find by invalid id should result in not found response`() {
        // GIVEN
        val invalidId = ObjectId.get()
        val invalidFindByIdRequest = findPassByIdRequest(invalidId)

        // WHEN
        val message =
            connection.requestWithTimeout(FIND_BY_ID, invalidFindByIdRequest.toByteArray(), Duration.ofSeconds(10))

        // THEN
        val expectedResponse = failureFindPassByIdResponseWithPassNotFound(invalidId)
        val actualResponse = FindPassByIdResponse.parser().parseFrom(message.get().data)
        assertThat(actualResponse).isEqualTo(expectedResponse)
    }
}
