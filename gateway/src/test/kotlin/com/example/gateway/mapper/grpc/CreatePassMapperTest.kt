package com.example.gateway.mapper.grpc

import com.example.core.exception.InternalRuntimeException
import com.example.core.exception.PassOwnerNotFoundException
import com.example.core.exception.PassTypeNotFoundException
import com.example.gateway.infrastructure.mapper.grpc.CreatePassMapper.toGrpcProto
import com.example.gateway.infrastructure.mapper.grpc.CreatePassMapper.toInternalProto
import com.example.gateway.util.PassGrpcProtoFixture.failureCreatePassResponseWithPassOwnerNotFound
import com.example.gateway.util.PassGrpcProtoFixture.failureCreatePassResponseWithPassTypeNotFound
import com.example.gateway.util.PassProtoFixture.protoPass
import com.example.grpcapi.reqrep.pass.CreatePassRequest
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import com.example.internal.input.reqreply.CreatePassRequest as InternalCreatePassRequest
import com.example.internal.input.reqreply.CreatePassResponse as InternalCreatePassResponse

internal class CreatePassMapperTest {
    @Test
    fun `converting grpc proto to internal should successfully map properties`() {
        // GIVEN
        val grpcRequest = CreatePassRequest.newBuilder()
            .setPassToCreate(protoPass)
            .build()

        // WHEN
        val internalFromGrpc = grpcRequest.toInternalProto()

        // THEN
        assertThat(internalFromGrpc).isEqualTo(
            InternalCreatePassRequest.newBuilder().setPassToCreate(protoPass).build()
        )
    }

    @Test
    fun `mapping error case owner not found by id from internal to grpc should result in throwing exception`() {
        // GIVEN
        val passOwnerId = ObjectId.get()
        val exceptionMessage = "Pass owner not found by id $passOwnerId"
        val errorCreatePassResponse = failureCreatePassResponseWithPassOwnerNotFound(exceptionMessage)

        // WHEN // THEN
        val exception = assertThrows<PassOwnerNotFoundException> { errorCreatePassResponse.toGrpcProto() }
        assertThat(exception.message).isEqualTo(exceptionMessage)
    }

    @Test
    fun `mapping error case pass type not found by id from internal to grpc should result in throwing exception`() {
        // GIVEN
        val passTypeId = ObjectId.get()
        val exceptionMessage = "Pass type not found by id $passTypeId"
        val errorCreatePassResponse = failureCreatePassResponseWithPassTypeNotFound(exceptionMessage)

        // WHEN // THEN
        val exception = assertThrows<PassTypeNotFoundException> { errorCreatePassResponse.toGrpcProto() }
        assertThat(exception.message).isEqualTo(exceptionMessage)
    }

    @Test
    fun `mapping error case not set by id from internal to grpc should result in throwing internal exception`() {
        // GIVEN
        val exceptionMessage = "Some internal exception message"
        val errorFindPassByIdResponse = InternalCreatePassResponse.newBuilder().apply {
            failureBuilder.message = exceptionMessage
        }.build()

        // WHEN // THEN
        val exception = assertThrows<InternalRuntimeException> { errorFindPassByIdResponse.toGrpcProto() }
        assertThat(exception.message).isEqualTo(exceptionMessage)
    }

    @Test
    fun `mapping empty message error case from internal to grpc should result in throwing internal exception`() {
        // GIVEN
        val errorFindPassByIdResponse = InternalCreatePassResponse.newBuilder().apply {
            failureBuilder
        }.build()

        // WHEN // THEN
        val exception = assertThrows<InternalRuntimeException> { errorFindPassByIdResponse.toGrpcProto() }
        assertThat(exception.message).isEmpty()
    }

    @Test
    fun `response not set when mapping from internal to grpc should result in throwing internal exception`() {
        // GIVEN
        val errorFindPassByIdResponse = InternalCreatePassResponse.getDefaultInstance()

        // WHEN // THEN
        val exception = assertThrows<InternalRuntimeException> { errorFindPassByIdResponse.toGrpcProto() }
        assertThat(exception.message).isEqualTo("Response must not be default instance.")
    }
}
