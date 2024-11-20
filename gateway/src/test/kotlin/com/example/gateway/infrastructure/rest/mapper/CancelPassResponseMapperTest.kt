package com.example.gateway.infrastructure.rest.mapper

import com.example.core.exception.InternalRuntimeException
import com.example.core.exception.PassOwnerNotFoundException
import com.example.gateway.infrastructure.rest.mapper.CancelPassResponseMapper.toUnitResponse
import com.example.gateway.util.PassProtoFixture.failureCancelPassResponse
import com.example.gateway.util.PassProtoFixture.failureCancelPassWithPassOwnerNotFoundResponse
import com.example.gateway.util.PassProtoFixture.succesfulCancelPassResponse
import com.example.internal.input.reqreply.CancelPassResponse
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class CancelPassResponseMapperTest {
    @Test
    fun `in case of successful completion should not throw exceptions`() {
        // GIVEN
        val response = succesfulCancelPassResponse

        // WHEN // THEN
        assertDoesNotThrow { response.toUnitResponse() }
    }

    @Test
    fun `in case of getting internal exception should throw internal exception`() {
        // GIVEN
        val internalException = IllegalArgumentException()
        val response = failureCancelPassResponse(internalException)

        // WHEN // THEN
        val exception = assertThrows<InternalRuntimeException> { response.toUnitResponse() }
        assertThat(exception.message).isEmpty()
    }

    @Test
    fun `in case of getting default instance should throw internal exception`() {
        // GIVEN
        val defaultInstanceResponse = CancelPassResponse.getDefaultInstance()

        // WHEN // THEN
        assertThrows<InternalRuntimeException> { defaultInstanceResponse.toUnitResponse() }
    }

    @Test
    fun `in case of getting pass owner not found exception message throw pass owner not found exception`() {
        // GIVEN
        val passOwnerId = ObjectId.get()
        val response = failureCancelPassWithPassOwnerNotFoundResponse(passOwnerId)

        // WHEN // THEN
        assertThrows<PassOwnerNotFoundException> { response.toUnitResponse() }
    }
}
