package com.example.gateway.grpc

import com.example.gateway.application.port.output.NatsHandlerPassMessageOutPort
import com.example.gateway.infrastructure.grpc.GrpcPassService
import com.example.gateway.infrastructure.rest.mapper.CreatePassResponseMapper.toCreatePassRequest
import com.example.gateway.util.PassDtoFixture.passDto
import com.example.gateway.util.PassDtoFixture.passId
import com.example.gateway.util.PassGrpcProtoFixture
import com.example.gateway.util.PassGrpcProtoFixture.protoPass
import com.example.gateway.util.PassProtoFixture
import com.example.gateway.util.PassProtoFixture.findPassByIdRequest
import com.example.grpcapi.reqrep.pass.CreatePassRequest
import com.example.grpcapi.reqrep.pass.FindPassByIdRequest
import com.example.grpcapi.reqrep.pass.GetAllTransferredPassesRequest
import com.example.internal.input.reqreply.TransferredPassMessage
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
internal class GrpcPassServiceTest {
    @MockK
    private lateinit var natsHandlerPassMessageOutPort: NatsHandlerPassMessageOutPort

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
            natsHandlerPassMessageOutPort.getAllTransferredPasses(passTypeName)
        } returns passesToReturn.toFlux()

        // WHEN
        val response = grpcPassService.getAllTransferredPasses(getAllTransferredPassesGrpcRequest)

        // THEN
        response.collectList()
            .test()
            .assertNext { passes ->
                assertThat(passes).hasSize(3)
                assertThat(passes).isEqualTo(passesToReturn.map { it.pass })
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
            natsHandlerPassMessageOutPort.createPass(passDto.toCreatePassRequest())
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
            natsHandlerPassMessageOutPort.findPassById(findPassByIdRequest(passId))
        } returns PassProtoFixture.successfulFindPassByIdResponse(PassProtoFixture.protoPass).toMono()

        // WHEN
        val findById = grpcPassService.findPassById(findByIdGrpcRequest)

        // THEN
        findById.test()
            .expectNext(PassGrpcProtoFixture.successfulFindPassByIdResponse(protoPass))
            .verifyComplete()
    }
}
