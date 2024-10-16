package com.example.passmanagersvc.web.controller.nats

import com.example.internal.NatsSubject.Pass.DELETE_BY_ID
import com.example.internal.input.reqreply.DeletePassByIdResponse
import com.example.passmanagersvc.repositories.PassRepository
import com.example.passmanagersvc.util.PassFixture.passToCreate
import com.example.passmanagersvc.util.PassProtoFixture.deletePassByIdRequest
import com.example.passmanagersvc.util.PassProtoFixture.succesfulDeletePassByIdResponse
import io.nats.client.Connection
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.Duration
import kotlin.test.Test

@ActiveProfiles("test")
@SpringBootTest
class DeletePassNatsControllerTest {
    @Autowired
    private lateinit var connection: Connection

    @Autowired
    private lateinit var passRepository: PassRepository

    @Test
    fun `deleting pass should return proto response with no content`() {
        // GIVEN
        val pass = passRepository.insert(passToCreate).block()

        val deletePassByIdRequest = deletePassByIdRequest(pass?.id)

        // WHEN
        val cancelMessage = connection.requestWithTimeout(
            DELETE_BY_ID,
            deletePassByIdRequest.toByteArray(),
            Duration.ofSeconds(10)
        )

        // THEN
        val expectedResponse = succesfulDeletePassByIdResponse
        val actualResponse = DeletePassByIdResponse.parser().parseFrom(cancelMessage.get().data)
        assertThat(actualResponse).isEqualTo(expectedResponse)
    }
}
