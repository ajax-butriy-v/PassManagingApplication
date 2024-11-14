package com.example.gateway.grpc

import com.example.gateway.infrastructure.grpc.GrpcPassService
import com.example.gateway.infrastructure.mapper.rest.CreatePassResponseMapper.toCreatePassRequest
import com.example.gateway.util.PassDtoFixture.passDto
import com.example.gateway.util.PassDtoFixture.passId
import com.example.gateway.util.PassGrpcProtoFixture
import com.example.gateway.util.PassGrpcProtoFixture.protoPass
import com.example.gateway.util.PassProtoFixture
import com.example.grpcapi.reqrep.pass.CreatePassRequest
import com.example.grpcapi.reqrep.pass.FindPassByIdRequest
import com.example.grpcapi.reqrep.pass.GetAllTransferredPassesRequest
import com.example.internal.NatsSubject
import com.example.internal.NatsSubject.Pass.CREATE
import com.example.internal.NatsSubject.Pass.FIND_BY_ID
import com.example.internal.input.reqreply.TransferredPassMessage
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.nats.client.Message
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test
import systems.ajax.nats.handler.api.NatsHandlerManager
import systems.ajax.nats.publisher.api.NatsMessagePublisher
import kotlin.test.Test
import com.example.internal.input.reqreply.CreatePassResponse as InternalCreatePassResponse
import com.example.internal.input.reqreply.FindPassByIdResponse as InternalFindPassByIdResponse

@ExtendWith(MockKExtension::class)
internal class GrpcPassServiceTest {
    @MockK
    private lateinit var natsHandlerManager: NatsHandlerManager

    @MockK
    private lateinit var natsMessagePublisher: NatsMessagePublisher

    @InjectMockKs
    private lateinit var grpcPassService: GrpcPassService

    @Test
    fun `get all transferred passes by pass type name should return flux from nats subject`() {
        // GIVEN
        val passTypeName = "Example"
        val getAllTransferredPassesGrpcRequest = GetAllTransferredPassesRequest.newBuilder()
            .setPassTypeName(passTypeName)
            .build()

        val passesToReturn = List(3) { TransferredPassMessage.getDefaultInstance() }

        every {
            natsHandlerManager.subscribe(
                NatsSubject.Pass.subjectByPassTypeName(passTypeName),
                any<(Message) -> TransferredPassMessage>()
            )
        } returns passesToReturn.toFlux()

        // WHEN
        val response = grpcPassService.getAllTransferredPasses(getAllTransferredPassesGrpcRequest)

        // THEN
        response.collectList()
            .test()
            .assertNext {
                assertThat(it).hasSize(3)
                assertThat(it).isEqualTo(passesToReturn.map { it.pass })
            }
            .verifyComplete()
    }

    @Test
    fun `creating pass should create a new pass`() {
        // GIVEN
        val createPassGrpcRequest = CreatePassRequest.newBuilder()
            .setPassToCreate(protoPass)
            .build()
        every {
            natsMessagePublisher.request(
                CREATE, passDto.toCreatePassRequest(), InternalCreatePassResponse.parser()
            )
        } returns PassProtoFixture.successfulCreatePassResponse(PassProtoFixture.protoPass).toMono()

        // WHEN
        val createdPass = grpcPassService.createPass(createPassGrpcRequest)

        // THEN
        createdPass.test()
            .expectNext(PassGrpcProtoFixture.successfulCreatePassResponse(protoPass))
            .verifyComplete()
    }

    @Test
    fun `find by id should return pass by id`() {
        // GIVEN
        val findByIdGrpcRequest = FindPassByIdRequest.newBuilder().setId(passId).build()
        every {
            natsMessagePublisher.request(
                FIND_BY_ID, PassProtoFixture.findPassByIdRequest(passId), InternalFindPassByIdResponse.parser()
            )
        } returns PassProtoFixture.successfulFindPassByIdResponse(PassProtoFixture.protoPass).toMono()

        // WHEN
        val findById = grpcPassService.findPassById(findByIdGrpcRequest)

        // THEN
        findById.test()
            .expectNext(PassGrpcProtoFixture.successfulFindPassByIdResponse(protoPass))
            .verifyComplete()
    }
}
