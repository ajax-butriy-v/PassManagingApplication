package com.example.gateway.web.mapper.proto.pass

import com.example.gateway.proto.PassDtoFixture
import com.example.gateway.web.mapper.proto.pass.CreatePassResponseMapper.toDto
import com.example.internal.input.reqreply.CreatePassResponse
import com.example.passmanagersvc.exception.PassOwnerNotFoundException
import com.example.passmanagersvc.exception.PassTypeNotFoundException
import com.example.passmanagersvc.util.PassProtoFixture.failureCreatePassResponseWithPassOwnerNotFound
import com.example.passmanagersvc.util.PassProtoFixture.failureCreatePassResponseWithPassTypeNotFound
import com.example.passmanagersvc.util.PassProtoFixture.successfulCreatePassResponse
import com.example.passmanagersvc.web.mapper.proto.pass.CreatePassMapper.failureCreatedPassResponse
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CreatePassResponseMapperTest {
    @Test
    fun `in case of successful completion should not throw exceptions`() {
        // GIVEN
        val response = successfulCreatePassResponse(PassDtoFixture.passFromDto)

        // WHEN // THEN
        assertThat(response.toDto()).isEqualTo(PassDtoFixture.passDto)
    }

    @Test
    fun `in case of getting internal exception should throw illegal state exception`() {
        // GIVEN
        val internalException = IllegalStateException()
        val response = failureCreatedPassResponse(internalException)

        // WHEN // THEN
        assertThrows<IllegalStateException> { response.toDto() }
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
        assertThrows<IllegalArgumentException> { defaultInstanceResponse.toDto() }
    }
}
