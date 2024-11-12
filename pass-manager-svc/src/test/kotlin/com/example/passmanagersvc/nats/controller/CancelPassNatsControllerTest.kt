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
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import reactor.kotlin.test.test
import systems.ajax.nats.publisher.api.NatsMessagePublisher
import kotlin.test.Test

internal class CancelPassNatsControllerTest : IntegrationTest() {
    @Autowired
    private lateinit var natsMessagePublisher: NatsMessagePublisher

    @Autowired
    private lateinit var passRepository: PassRepository

    @Autowired
    private lateinit var passOwnerRepository: PassOwnerRepository

    @Test
    fun `canceling pass should return successful proto response`() {
        // GIVEN
        val passOwner = passOwnerRepository.insert(getOwnerWithUniqueFields()).block()!!
        val pass = passRepository.insert(passToCreate.copy(passOwnerId = passOwner.id)).block()!!
        val expectedResponse = succesfulCancelPassResponse
        val cancelPassRequest = cancelPassRequest(pass.id.toString(), passOwner.id.toString())

        // WHEN
        val actualResponse = natsMessagePublisher.request(
            CANCEL,
            cancelPassRequest,
            CancelPassResponse.parser()
        )


        // THEN
        actualResponse.test()
            .expectNext(expectedResponse)
            .verifyComplete()
    }

    @Test
    fun `canceling pass with non-existent pass owner should return pass owner not found response`() {
        // GIVEN
        val invalidOwnerId = ObjectId.get().toString()
        val invalidCancelPassRequest = cancelPassRequest(ObjectId.get().toString(), invalidOwnerId)
        val expectedResponse = failureCancelPassResponseWithOwnerNotFound(invalidOwnerId)

        // WHEN
        val invalidCancelMessage = natsMessagePublisher.request(
            CANCEL,
            invalidCancelPassRequest,
            CancelPassResponse.parser()
        )

        // THEN
        invalidCancelMessage.test()
            .expectNext(expectedResponse)
            .verifyComplete()
    }
}
