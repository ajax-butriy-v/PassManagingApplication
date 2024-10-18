package com.example.passmanagersvc.nats.controller

import com.example.internal.NatsSubject.Pass.DELETE_BY_ID
import com.example.internal.input.reqreply.DeletePassByIdResponse
import com.example.passmanagersvc.repositories.PassRepository
import com.example.passmanagersvc.util.IntegrationTest
import com.example.passmanagersvc.util.PassFixture.passToCreate
import com.example.passmanagersvc.util.PassProtoFixture.deletePassByIdRequest
import com.example.passmanagersvc.util.PassProtoFixture.succesfulDeletePassByIdResponse
import io.nats.client.Connection
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import kotlin.test.Test

internal class DeletePassNatsControllerTest : IntegrationTest() {
    @Autowired
    private lateinit var connection: Connection

    @Autowired
    private lateinit var passRepository: PassRepository

    @Test
    fun `deleting pass should return proto response with no content`() {
        // GIVEN
        val pass = passRepository.insert(passToCreate).block()!!
        val expectedResponse = succesfulDeletePassByIdResponse
        val deletePassByIdRequest = deletePassByIdRequest(pass.id.toString())

        // WHEN
        val cancelMessage = connection.requestWithTimeout(
            DELETE_BY_ID,
            deletePassByIdRequest.toByteArray(),
            Duration.ofSeconds(10)
        )

        // THEN
        val actualResponse = DeletePassByIdResponse.parser().parseFrom(cancelMessage.get().data)
        assertThat(actualResponse).isEqualTo(expectedResponse)
    }
}
