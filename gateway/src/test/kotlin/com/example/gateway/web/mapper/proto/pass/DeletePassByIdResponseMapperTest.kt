package com.example.gateway.web.mapper.proto.pass

import com.example.gateway.web.mapper.proto.pass.DeletePassByIdResponseMapper.toDeleteResponse
import com.example.passmanagersvc.input.reqreply.DeletePassByIdResponse
import com.example.passmanagersvc.util.PassProtoFixture.succesfulDeletePassByIdResponse
import com.example.passmanagersvc.web.mapper.proto.pass.DeletePassByIdMapper.failureDeletePassByIdResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class DeletePassByIdResponseMapperTest {

    @Test
    fun `in case of successful completion should not throw exceptions`() {
        // GIVEN
        val response: DeletePassByIdResponse = succesfulDeletePassByIdResponse

        // WHEN // THEN
        assertDoesNotThrow { response.toDeleteResponse() }
    }

    @Test
    fun `in case of getting internal exception should throw illegal state exception`() {
        // GIVEN
        val internalException = IllegalStateException()
        val response = failureDeletePassByIdResponse(internalException)

        // WHEN // THEN
        assertThrows<IllegalStateException> { response.toDeleteResponse() }
    }

    @Test
    fun `in case of getting default instance should throw illegal argument exception`() {
        // GIVEN
        val defaultInstanceResponse = DeletePassByIdResponse.getDefaultInstance()

        // WHEN // THEN
        assertThrows<IllegalArgumentException> { defaultInstanceResponse.toDeleteResponse() }
    }
}
