package com.example.passmanagersvc.nats.controller

import com.example.internal.NatsSubject.Pass.CANCEL
import com.example.internal.input.reqreply.CancelPassResponse
import com.example.passmanagersvc.repositories.PassOwnerRepository
import com.example.passmanagersvc.repositories.PassRepository
import com.example.passmanagersvc.util.IntegrationTest
import com.example.passmanagersvc.util.PassFixture.passToCreate
import com.example.passmanagersvc.util.PassOwnerFixture.getOwnerWithUniqueFields
import com.example.passmanagersvc.util.PassProtoFixture.cancelPassRequest
import com.example.passmanagersvc.util.PassProtoFixture.failureCancelPassResponseWithOwnerNotFound
import com.example.passmanagersvc.util.PassProtoFixture.succesfulCancelPassResponse
import io.nats.client.Connection
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import java.time.Duration
import kotlin.test.Test

internal class CancelPassNatsControllerTest : IntegrationTest() {
    @Autowired
    private lateinit var connection: Connection

    @Autowired
    private lateinit var passRepository: PassRepository

    @Autowired
    private lateinit var passOwnerRepository: PassOwnerRepository

    @Test
    fun `canceling pass should return successful proto response`() {
        // GIVEN
        val passOwner = passOwnerRepository.insert(getOwnerWithUniqueFields()).block()!!
        val pass = passRepository.insert(passToCreate).block()!!
        val expectedResponse = succesfulCancelPassResponse
        val cancelPassRequest = cancelPassRequest(pass.id.toString(), passOwner.id.toString())

        // WHEN
        val cancelMessage = connection.requestWithTimeout(
            CANCEL,
            cancelPassRequest.toByteArray(),
            Duration.ofSeconds(10)
        )

        // THEN
        val actualResponse = CancelPassResponse.parser().parseFrom(cancelMessage.get().data)
        assertThat(actualResponse).isEqualTo(expectedResponse)
    }

    @Test
    fun `canceling pass with non-existent pass owner should return pass owner not found response`() {
        // GIVEN
        val invalidOwnerId = ObjectId.get().toString()
        val invalidCancelPassRequest = cancelPassRequest(ObjectId.get().toString(), invalidOwnerId)
        val exceptedResponse = failureCancelPassResponseWithOwnerNotFound(invalidOwnerId)

        // WHEN
        val invalidCancelMessage = connection.requestWithTimeout(
            CANCEL,
            invalidCancelPassRequest.toByteArray(),
            Duration.ofSeconds(10)
        )

        // THEN
        val actualResponse = CancelPassResponse.parser().parseFrom(invalidCancelMessage.get().data)
        assertThat(actualResponse).isEqualTo(exceptedResponse)
    }
}
