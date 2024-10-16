package com.example.passmanagersvc.web.controller.nats

import com.example.internal.NatsSubject.Pass.TRANSFER
import com.example.passmanagersvc.input.reqreply.TransferPassResponse
import com.example.passmanagersvc.repositories.PassOwnerRepository
import com.example.passmanagersvc.repositories.PassRepository
import com.example.passmanagersvc.util.PassFixture.passToCreate
import com.example.passmanagersvc.util.PassOwnerFixture.getOwnerWithUniqueFields
import com.example.passmanagersvc.util.PassProtoFixture.failureTransferPassResponseWithPassNotFound
import com.example.passmanagersvc.util.PassProtoFixture.failureTransferPassResponseWithPassOwnerNotFound
import com.example.passmanagersvc.util.PassProtoFixture.successfulTransferPassResponse
import com.example.passmanagersvc.util.PassProtoFixture.transferPassRequest
import io.nats.client.Connection
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.Duration
import kotlin.test.Test

@ActiveProfiles("test")
@SpringBootTest
class TransferPassNatsControllerTest {
    @Autowired
    private lateinit var connection: Connection

    @Autowired
    private lateinit var passRepository: PassRepository

    @Autowired
    private lateinit var passOwnerRepository: PassOwnerRepository

    @Test
    fun `transferring pass should return successful proto response`() {
        // GIVEN
        val passOwner = passOwnerRepository.insert(
            getOwnerWithUniqueFields()
        ).block()
        val pass = passRepository.insert(passToCreate).block()

        val transferPassRequest = transferPassRequest(pass?.id, passOwner?.id)

        // WHEN
        val transferMessage = connection.requestWithTimeout(
            TRANSFER,
            transferPassRequest.toByteArray(),
            Duration.ofSeconds(10)
        )

        // THEN
        val exceptedResponse = successfulTransferPassResponse
        val actualResponse = TransferPassResponse.parser().parseFrom(transferMessage.get().data)
        assertThat(actualResponse).isEqualTo(exceptedResponse)
    }

    @Test
    fun `transferring pass with invalid pass id should result in not found response`() {
        // GIVEN
        val passOwner = passOwnerRepository.insert(
            getOwnerWithUniqueFields()
        ).block()
        val invalidPassId = ObjectId.get()
        val transferPassRequest = transferPassRequest(invalidPassId, passOwner?.id)

        // WHEN
        val transferMessage = connection.requestWithTimeout(
            TRANSFER,
            transferPassRequest.toByteArray(),
            Duration.ofSeconds(10)
        )

        // THEN
        val exceptedResponse = failureTransferPassResponseWithPassNotFound(invalidPassId)
        val actualResponse = TransferPassResponse.parser().parseFrom(transferMessage.get().data)
        assertThat(actualResponse).isEqualTo(exceptedResponse)
    }

    @Test
    fun `transferring pass with invalid pass owner id should result in not found response`() {
        // GIVEN
        val pass = passRepository.insert(passToCreate).block()

        val invalidPassOwnerId = ObjectId.get()
        val transferPassRequest = transferPassRequest(pass?.id, invalidPassOwnerId)

        // WHEN
        val transferMessage = connection.requestWithTimeout(
            TRANSFER,
            transferPassRequest.toByteArray(),
            Duration.ofSeconds(10)
        )

        // THEN
        val exceptedResponse = failureTransferPassResponseWithPassOwnerNotFound(invalidPassOwnerId)
        val actualResponse = TransferPassResponse.parser().parseFrom(transferMessage.get().data)
        assertThat(actualResponse).isEqualTo(exceptedResponse)
    }
}
