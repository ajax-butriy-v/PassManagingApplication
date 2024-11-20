package com.example.gateway.infrastructure.rest.mapper

import com.example.core.exception.InternalRuntimeException
import com.example.core.exception.PassNotFoundException
import com.example.core.exception.PassOwnerNotFoundException
import com.example.gateway.infrastructure.rest.mapper.TransferPassResponseMapper.toUnitResponse
import com.example.gateway.util.PassProtoFixture.failureTransferPassResponse
import com.example.gateway.util.PassProtoFixture.failureTransferPassResponseWithPassNotFound
import com.example.gateway.util.PassProtoFixture.failureTransferPassResponseWithPassOwnerNotFound
import com.example.gateway.util.PassProtoFixture.successfulTransferPassResponse
import com.example.internal.input.reqreply.TransferPassResponse
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class TransferPassResponseMapperTest {
    @Test
    fun `in case of successful completion should not throw exceptions`() {
        // GIVEN
        val response = successfulTransferPassResponse

        // WHEN // THEN
        assertDoesNotThrow { response.toUnitResponse() }
    }

    @Test
    fun `in case of getting internal exception should throw internal exception`() {
        // GIVEN
        val internalException = IllegalStateException()
        val response = failureTransferPassResponse(internalException)

        // WHEN // THEN
        val exception = assertThrows<InternalRuntimeException> { response.toUnitResponse() }
        assertThat(exception.message).isEmpty()
    }

    @Test
    fun `in case of getting pass not found exception message throw pass not found exception`() {
        // GIVEN
        val passId = ObjectId.get()
        val response = failureTransferPassResponseWithPassNotFound(passId)

        // WHEN // THEN
        assertThrows<PassNotFoundException> { response.toUnitResponse() }
    }

    @Test
    fun `in case of getting pass owner not found exception message throw pass owner not found exception`() {
        // GIVEN
        val passId = ObjectId.get()
        val response = failureTransferPassResponseWithPassOwnerNotFound(passId)

        // WHEN // THEN
        assertThrows<PassOwnerNotFoundException> { response.toUnitResponse() }
    }

    @Test
    fun `in case of getting default instance should throw  internal exception`() {
        // GIVEN
        val defaultInstanceResponse = TransferPassResponse.getDefaultInstance()

        // WHEN // THEN
        assertThrows<InternalRuntimeException> { defaultInstanceResponse.toUnitResponse() }
    }
}
