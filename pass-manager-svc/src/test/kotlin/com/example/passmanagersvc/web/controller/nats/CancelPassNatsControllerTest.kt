package com.example.passmanagersvc.web.controller.nats

import com.example.internal.NatsSubject.Pass.CANCEL
import com.example.internal.input.reqreply.CancelPassResponse
import com.example.passmanagersvc.repositories.PassOwnerRepository
import com.example.passmanagersvc.repositories.PassRepository
import com.example.passmanagersvc.util.PassFixture.passToCreate
import com.example.passmanagersvc.util.PassOwnerFixture.getOwnerWithUniqueFields
import com.example.passmanagersvc.util.PassProtoFixture.cancelPassRequest
import com.example.passmanagersvc.util.PassProtoFixture.failureCancelPassResponseWithOwnerNotFound
import com.example.passmanagersvc.util.PassProtoFixture.succesfulCancelPassResponse
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
class CancelPassNatsControllerTest {
    @Autowired
    private lateinit var connection: Connection

    @Autowired
    private lateinit var passRepository: PassRepository

    @Autowired
    private lateinit var passOwnerRepository: PassOwnerRepository

    @Test
    fun `canceling pass should return successful proto response`() {
        // GIVEN
        val passOwner = passOwnerRepository.insert(getOwnerWithUniqueFields()).block()
        val pass = passRepository.insert(passToCreate).block()

        val cancelPassRequest = cancelPassRequest(pass!!.id, passOwner!!.id)

        // WHEN
        val cancelMessage = connection.requestWithTimeout(
            CANCEL,
            cancelPassRequest.toByteArray(),
            Duration.ofSeconds(10)
        )

        // THEN
        val expectedResponse = succesfulCancelPassResponse
        val actualResponse = CancelPassResponse.parser().parseFrom(cancelMessage.get().data)
        assertThat(actualResponse).isEqualTo(expectedResponse)
    }

    @Test
    fun `canceling pass with non-existent pass owner should return pass owner not found response`() {
        // GIVEN
        val invalidOwnerId = ObjectId.get()
        val invalidCancelPassRequest = cancelPassRequest(ObjectId.get(), invalidOwnerId)

        // WHEN
        val invalidCancelMessage = connection.requestWithTimeout(
            CANCEL,
            invalidCancelPassRequest.toByteArray(),
            Duration.ofSeconds(10)
        )

        // THEN
        val exceptedResponse = failureCancelPassResponseWithOwnerNotFound(invalidOwnerId)
        val actualResponse = CancelPassResponse.parser().parseFrom(invalidCancelMessage.get().data)
        assertThat(actualResponse).isEqualTo(exceptedResponse)
    }
}
