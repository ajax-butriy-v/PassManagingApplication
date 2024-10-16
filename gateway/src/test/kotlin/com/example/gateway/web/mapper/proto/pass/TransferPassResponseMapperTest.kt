package com.example.gateway.web.mapper.proto.pass

import com.example.gateway.web.mapper.proto.pass.TransferPassResponseMapper.toTransferResponse
import com.example.passmanagersvc.exception.PassNotFoundException
import com.example.passmanagersvc.exception.PassOwnerNotFoundException
import com.example.passmanagersvc.input.reqreply.TransferPassResponse
import com.example.passmanagersvc.util.PassProtoFixture.failureTransferPassResponseWithPassNotFound
import com.example.passmanagersvc.util.PassProtoFixture.failureTransferPassResponseWithPassOwnerNotFound
import com.example.passmanagersvc.util.PassProtoFixture.successfulTransferPassResponse
import com.example.passmanagersvc.web.mapper.proto.pass.TransferPassMapper.failureTransferPassResponse
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class TransferPassResponseMapperTest {
    @Test
    fun `in case of successful completion should not throw exceptions`() {
        // GIVEN
        val response = successfulTransferPassResponse

        // WHEN // THEN
        assertDoesNotThrow { response.toTransferResponse() }
    }

    @Test
    fun `in case of getting internal exception should throw illegal state exception`() {
        // GIVEN
        val internalException = IllegalStateException()
        val response = failureTransferPassResponse(internalException)

        // WHEN // THEN
        assertThrows<IllegalStateException> { response.toTransferResponse() }
    }

    @Test
    fun `in case of getting pass not found exception message throw pass not found exception`() {
        // GIVEN
        val passId = ObjectId.get()
        val response = failureTransferPassResponseWithPassNotFound(passId)

        // WHEN // THEN
        assertThrows<PassNotFoundException> { response.toTransferResponse() }
    }

    @Test
    fun `in case of getting pass owner not found exception message throw pass owner not found exception`() {
        // GIVEN
        val passId = ObjectId.get()
        val response = failureTransferPassResponseWithPassOwnerNotFound(passId)

        // WHEN // THEN
        assertThrows<PassOwnerNotFoundException> { response.toTransferResponse() }
    }

    @Test
    fun `in case of getting default instance should throw illegal argument exception`() {
        // GIVEN
        val defaultInstanceResponse = TransferPassResponse.getDefaultInstance()

        // WHEN // THEN
        assertThrows<IllegalArgumentException> { defaultInstanceResponse.toTransferResponse() }
    }
}
