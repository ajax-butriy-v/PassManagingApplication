package com.example.passmanagersvc.pass.nats.handler

import com.example.internal.NatsSubject.Pass.TRANSFER
import com.example.internal.input.reqreply.TransferPassResponse
import com.example.passmanagersvc.pass.application.port.output.PassRepositoryOutPort
import com.example.passmanagersvc.pass.util.IntegrationTest
import com.example.passmanagersvc.passowner.application.port.out.PassOwnerRepositoryOutPort
import com.example.passmanagersvc.passowner.infrastructure.mongo.mapper.PassOwnerMapper.toDomain
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
    private lateinit var passRepository: PassRepositoryOutPort

    @Autowired
    private lateinit var passOwnerRepository: PassOwnerRepositoryOutPort

    @Test
    fun `transferring pass should return successful proto response`() {
        // GIVEN
        val passOwner = passOwnerRepository.insert(
            getOwnerWithUniqueFields().toDomain()
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
            getOwnerWithUniqueFields().toDomain()
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
