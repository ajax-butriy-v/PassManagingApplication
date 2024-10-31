package com.example.gateway.mapper.rest

import com.example.core.exception.InternalRuntimeException
import com.example.core.exception.PassNotFoundException
import com.example.gateway.mapper.rest.FindPassByIdResponseMapper.toDto
import com.example.gateway.util.PassDtoFixture
import com.example.gateway.util.PassProtoFixture.failureFindPassByIdResponseWithPassNotFound
import com.example.gateway.util.PassProtoFixture.protoPass
import com.example.gateway.util.PassProtoFixture.successfulFindPassByIdResponse
import com.example.internal.input.reqreply.FindPassByIdResponse
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FindPassByIdMapperTest {
    @Test
    fun `in case of successful completion should map response to pass dto`() {
        // GIVEN
        val response = successfulFindPassByIdResponse(protoPass)

        // WHEN // THEN
        assertThat(response.toDto()).isEqualTo(PassDtoFixture.passDto)
    }

    @Test
    fun `in case of getting internal exception should throw internal exception`() {
        // GIVEN
        val internalException = IllegalStateException()
        val response = FindPassByIdResponse.newBuilder().apply {
            failureBuilder.setMessage(internalException.message.orEmpty())
        }.build()

        // WHEN // THEN
        val exception = assertThrows<InternalRuntimeException> { response.toDto() }
        assertThat(exception.message).isEmpty()
    }

    @Test
    fun `in case of getting pass type not found exception message throw pass not found exception`() {
        // GIVEN
        val passId = ObjectId.get().toString()
        val response = failureFindPassByIdResponseWithPassNotFound(passId)

        // WHEN // THEN
        assertThrows<PassNotFoundException> { response.toDto() }
    }

    @Test
    fun `in case of getting default instance should throw  nternal exception`() {
        // GIVEN
        val defaultInstanceResponse = FindPassByIdResponse.getDefaultInstance()

        // WHEN // THEN
        assertThrows<InternalRuntimeException> { defaultInstanceResponse.toDto() }
    }
}
