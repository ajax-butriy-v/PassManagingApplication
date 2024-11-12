package com.example.passmanagersvc.nats.controller

import com.example.internal.NatsSubject.Pass.DELETE_BY_ID
import com.example.internal.input.reqreply.DeletePassByIdResponse
import com.example.passmanagersvc.repositories.PassRepository
import com.example.passmanagersvc.util.IntegrationTest
import com.example.passmanagersvc.util.PassFixture.passToCreate
import com.example.passmanagersvc.util.PassProtoFixture.deletePassByIdRequest
import com.example.passmanagersvc.util.PassProtoFixture.succesfulDeletePassByIdResponse
import org.springframework.beans.factory.annotation.Autowired
import reactor.kotlin.test.test
import systems.ajax.nats.publisher.api.NatsMessagePublisher
import kotlin.test.Test

internal class DeletePassNatsControllerTest : IntegrationTest() {
    @Autowired
    private lateinit var natsMessagePublisher: NatsMessagePublisher

    @Autowired
    private lateinit var passRepository: PassRepository

    @Test
    fun `deleting pass should return proto response with no content`() {
        // GIVEN
        val pass = passRepository.insert(passToCreate).block()!!
        val expectedResponse = succesfulDeletePassByIdResponse
        val deletePassByIdRequest = deletePassByIdRequest(pass.id.toString())

        // WHEN
        val cancelMessage = natsMessagePublisher.request(
            DELETE_BY_ID,
            deletePassByIdRequest,
            DeletePassByIdResponse.parser()
        )

        // THEN
        cancelMessage.test()
            .expectNext(expectedResponse)
            .verifyComplete()
    }
}
