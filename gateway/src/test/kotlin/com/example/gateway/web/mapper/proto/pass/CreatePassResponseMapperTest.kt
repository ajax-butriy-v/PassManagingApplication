package com.example.gateway.web.mapper.proto.pass

import com.example.core.exception.InternalRuntimeException
import com.example.core.exception.PassOwnerNotFoundException
import com.example.core.exception.PassTypeNotFoundException
import com.example.gateway.util.PassDtoFixture
import com.example.gateway.util.PassProtoFixture.failureCreatePassResponse
import com.example.gateway.util.PassProtoFixture.failureCreatePassResponseWithPassOwnerNotFound
import com.example.gateway.util.PassProtoFixture.failureCreatePassResponseWithPassTypeNotFound
import com.example.gateway.util.PassProtoFixture.protoPass
import com.example.gateway.util.PassProtoFixture.successfulCreatePassResponse
import com.example.gateway.web.mapper.proto.pass.CreatePassResponseMapper.toDto
import com.example.internal.input.reqreply.CreatePassResponse
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CreatePassResponseMapperTest {
    @Test
    fun `in case of successful completion should not throw exceptions`() {
        // GIVEN
        val response = successfulCreatePassResponse(protoPass)

        // WHEN // THEN
        assertThat(response.toDto()).isEqualTo(PassDtoFixture.passDto)
    }

    @Test
    fun `in case of getting internal exception should throw internal exception`() {
        // GIVEN
        val internalException = IllegalStateException()
        val response = failureCreatePassResponse(internalException)

        // WHEN // THEN
        val exception = assertThrows<InternalRuntimeException> { response.toDto() }
        assertThat(exception.message).isEmpty()
    }

    @Test
    fun `in case of getting pass type not found exception message throw pass not found exception`() {
        // GIVEN
        val passTypeId = ObjectId.get()
        val response = failureCreatePassResponseWithPassTypeNotFound(passTypeId)

        // WHEN // THEN
        assertThrows<PassTypeNotFoundException> { response.toDto() }
    }

    @Test
    fun `in case of getting pass owner not found exception message throw pass owner not found exception`() {
        // GIVEN
        val passOwnerId = ObjectId.get().toString()
        val response = failureCreatePassResponseWithPassOwnerNotFound(passOwnerId)

        // WHEN // THEN
        assertThrows<PassOwnerNotFoundException> { response.toDto() }
    }

    @Test
    fun `in case of getting default instance should throw illegal argument exception`() {
        // GIVEN
        val defaultInstanceResponse = CreatePassResponse.getDefaultInstance()

        // WHEN // THEN
        assertThrows<InternalRuntimeException> { defaultInstanceResponse.toDto() }
    }
}
