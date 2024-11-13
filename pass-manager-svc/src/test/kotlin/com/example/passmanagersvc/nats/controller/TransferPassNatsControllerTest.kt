package com.example.passmanagersvc.nats.controller

import com.example.internal.NatsSubject.Pass.TRANSFER
import com.example.internal.input.reqreply.TransferPassResponse
import com.example.passmanagersvc.repositories.PassOwnerRepository
import com.example.passmanagersvc.repositories.PassRepository
import com.example.passmanagersvc.util.IntegrationTest
import com.example.passmanagersvc.util.PassFixture.passToCreate
import com.example.passmanagersvc.util.PassOwnerFixture.getOwnerWithUniqueFields
import com.example.passmanagersvc.util.PassProtoFixture.failureTransferPassResponseWithPassNotFound
import com.example.passmanagersvc.util.PassProtoFixture.failureTransferPassResponseWithPassOwnerNotFound
import com.example.passmanagersvc.util.PassProtoFixture.successfulTransferPassResponse
import com.example.passmanagersvc.util.PassProtoFixture.transferPassRequest
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import reactor.kotlin.test.test
import systems.ajax.nats.publisher.api.NatsMessagePublisher
import kotlin.test.Test

internal class TransferPassNatsControllerTest : IntegrationTest() {
    @Autowired
    private lateinit var natsMessagePublisher: NatsMessagePublisher

    @Autowired
    private lateinit var passRepository: PassRepository

    @Autowired
    private lateinit var passOwnerRepository: PassOwnerRepository

    @Test
    fun `transferring pass should return successful proto response`() {
        // GIVEN
        val passOwner = passOwnerRepository.insert(
            getOwnerWithUniqueFields()
        ).block()!!
        val pass = passRepository.insert(passToCreate).block()!!
        val expectedResponse = successfulTransferPassResponse
        val transferPassRequest = transferPassRequest(pass.id.toString(), passOwner.id.toString())

        // WHEN
        val transferMessage = natsMessagePublisher.request(
            TRANSFER,
            transferPassRequest,
            TransferPassResponse.parser()
        )

        // THEN
        transferMessage.test()
            .expectNext(expectedResponse)
            .verifyComplete()
    }

    @Test
    fun `transferring pass with invalid pass id should result in not found response`() {
        // GIVEN
        val passOwner = passOwnerRepository.insert(
            getOwnerWithUniqueFields()
        ).block()!!
        val invalidPassId = ObjectId.get().toString()
        val expectedResponse = failureTransferPassResponseWithPassNotFound(invalidPassId)
        val transferPassRequest = transferPassRequest(invalidPassId, passOwner.id.toString())

        // WHEN
        val transferMessage = natsMessagePublisher.request(
            TRANSFER,
            transferPassRequest,
            TransferPassResponse.parser()
        )

        // THEN
        transferMessage.test()
            .expectNext(expectedResponse)
            .verifyComplete()
    }

    @Test
    fun `transferring pass with invalid pass owner id should result in not found response`() {
        // GIVEN
        val pass = passRepository.insert(passToCreate).block()!!
        val invalidPassOwnerId = ObjectId.get().toString()
        val expectedResponse = failureTransferPassResponseWithPassOwnerNotFound(invalidPassOwnerId)
        val transferPassRequest = transferPassRequest(pass.id.toString(), invalidPassOwnerId)

        // WHEN
        val transferMessage = natsMessagePublisher.request(
            TRANSFER,
            transferPassRequest,
            TransferPassResponse.parser()
        )

        // THEN
        transferMessage.test()
            .expectNext(expectedResponse)
            .verifyComplete()
    }
}
