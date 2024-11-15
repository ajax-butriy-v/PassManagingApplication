package com.example.passmanagersvc.nats.handler

import com.example.internal.NatsSubject.Pass.FIND_BY_ID
import com.example.internal.input.reqreply.FindPassByIdResponse
import com.example.passmanagersvc.application.port.output.PassRepositoryOutPort
import com.example.passmanagersvc.infrastructure.kafka.mapper.TransferredPassMessageMapper.toProto
import com.example.passmanagersvc.infrastructure.nats.mapper.FindPassByIdMapper.toSuccessFindPassByIdResponse
import com.example.passmanagersvc.util.IntegrationTest
import com.example.passmanagersvc.util.PassFixture.passToCreate
import com.example.passmanagersvc.util.PassProtoFixture.failureFindPassByIdResponseWithPassNotFound
import com.example.passmanagersvc.util.PassProtoFixture.findPassByIdRequest
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import reactor.kotlin.test.test
import systems.ajax.nats.publisher.api.NatsMessagePublisher
import kotlin.test.Test

internal class FindByIdNatsControllerTest : IntegrationTest() {
    @Autowired
    private lateinit var natsMessagePublisher: NatsMessagePublisher

    @Autowired
    private lateinit var passRepository: PassRepositoryOutPort

    @Test
    fun `find by id should return corresponding valid proto response`() {
        // GIVEN
        val pass = passRepository.insert(passToCreate).block()!!
        val findByIdRequest = findPassByIdRequest(pass.id.toString())
        val expectedResponse = pass.toProto().toSuccessFindPassByIdResponse()

        // WHEN
        val message = natsMessagePublisher.request(
            FIND_BY_ID,
            findByIdRequest,
            FindPassByIdResponse.parser()
        )

        // THEN
        message.test()
            .expectNext(expectedResponse)
            .verifyComplete()
    }

    @Test
    fun `find by invalid id should result in not found response`() {
        // GIVEN
        val invalidId = ObjectId.get().toString()
        val invalidFindByIdRequest = findPassByIdRequest(invalidId)
        val expectedResponse = failureFindPassByIdResponseWithPassNotFound(invalidId)

        // WHEN
        val message = natsMessagePublisher.request(
            FIND_BY_ID,
            invalidFindByIdRequest,
            FindPassByIdResponse.parser()
        )

        // THEN
        message.test()
            .expectNext(expectedResponse)
            .verifyComplete()
    }
}
