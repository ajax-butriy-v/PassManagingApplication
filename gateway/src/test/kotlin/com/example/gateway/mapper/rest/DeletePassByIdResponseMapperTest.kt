package com.example.gateway.mapper.rest

import com.example.core.exception.InternalRuntimeException
import com.example.gateway.infrastructure.rest.mapper.DeletePassByIdResponseMapper.toDeleteResponse
import com.example.gateway.util.PassProtoFixture.failureDeletePassByIdResponse
import com.example.gateway.util.PassProtoFixture.succesfulDeletePassByIdResponse
import com.example.internal.input.reqreply.DeletePassByIdResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class DeletePassByIdResponseMapperTest {

    @Test
    fun `in case of successful completion should not throw exceptions`() {
        // GIVEN
        val response: DeletePassByIdResponse = succesfulDeletePassByIdResponse

        // WHEN // THEN
        assertDoesNotThrow { response.toDeleteResponse() }
    }

    @Test
    fun `in case of getting internal exception should throw  internal exception`() {
        // GIVEN
        val internalException = IllegalArgumentException()
        val response = failureDeletePassByIdResponse(internalException)

        // WHEN // THEN
        val exception = assertThrows<InternalRuntimeException> { response.toDeleteResponse() }
        assertThat(exception.message).isEmpty()
    }

    @Test
    fun `in case of getting default instance should throw internal exception`() {
        // GIVEN
        val defaultInstanceResponse = DeletePassByIdResponse.getDefaultInstance()

        // WHEN // THEN
        assertThrows<InternalRuntimeException> { defaultInstanceResponse.toDeleteResponse() }
    }
}
