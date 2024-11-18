package com.example.gateway.mapper.grpc

import com.example.core.exception.InternalRuntimeException
import com.example.core.exception.PassNotFoundException
import com.example.gateway.infrastructure.grpc.mapper.FindPassByIdMapper.toGrpcProto
import com.example.gateway.infrastructure.grpc.mapper.FindPassByIdMapper.toInternalProto
import com.example.gateway.util.PassGrpcProtoFixture.failureFindPassByIdResponseWithPassNotFound
import com.example.grpcapi.reqrep.pass.FindPassByIdRequest
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import com.example.internal.input.reqreply.FindPassByIdResponse as InternalFindPassByIdResponse

internal class FindPassByIdMapperTest {

    @Test
    fun `converting grpc proto to internal should successfully map properties`() {
        // GIVEN
        val passId = ObjectId.get().toString()
        val grpcRequest = FindPassByIdRequest.newBuilder()
            .setId(passId)
            .build()

        // WHEN
        val internalFromGrpc = grpcRequest.toInternalProto()

        // THEN
        assertThat(internalFromGrpc.id).isEqualTo(passId)
    }

    @Test
    fun `mapping error case not found by id from internal to grpc should result in throwing exception`() {
        // GIVEN
        val passId = ObjectId.get()
        val exceptionMessage = "Pass not found by id $passId"
        val errorFindPassByIdResponse = failureFindPassByIdResponseWithPassNotFound(exceptionMessage)

        // WHEN // THEN
        val exception = assertThrows<PassNotFoundException> { errorFindPassByIdResponse.toGrpcProto() }
        assertThat(exception.message).isEqualTo(exceptionMessage)
    }

    @Test
    fun `mapping error case not set by id from internal to grpc should result in throwing internal exception`() {
        // GIVEN
        val exceptionMessage = "Some internal exception message"
        val errorFindPassByIdResponse = InternalFindPassByIdResponse.newBuilder().apply {
            failureBuilder.message = exceptionMessage
        }.build()

        // WHEN // THEN
        val exception = assertThrows<InternalRuntimeException> { errorFindPassByIdResponse.toGrpcProto() }
        assertThat(exception.message).isEqualTo(exceptionMessage)
    }

    @Test
    fun `mapping empty message error case from internal to grpc should result in throwing internal exception`() {
        // GIVEN
        val errorFindPassByIdResponse = InternalFindPassByIdResponse.newBuilder().apply {
            failureBuilder
        }.build()

        // WHEN // THEN
        val exception = assertThrows<InternalRuntimeException> { errorFindPassByIdResponse.toGrpcProto() }
        assertThat(exception.message).isEmpty()
    }

    @Test
    fun `response not set when mapping from internal to grpc should result in throwing internal exception`() {
        // GIVEN
        val errorFindPassByIdResponse = InternalFindPassByIdResponse.getDefaultInstance()

        // WHEN // THEN
        val exception = assertThrows<InternalRuntimeException> { errorFindPassByIdResponse.toGrpcProto() }
        assertThat(exception.message).isEqualTo("Response must not be default instance.")
    }
}
