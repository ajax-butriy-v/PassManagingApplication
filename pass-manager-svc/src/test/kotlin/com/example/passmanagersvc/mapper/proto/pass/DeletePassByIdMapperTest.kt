package com.example.passmanagersvc.mapper.proto.pass

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test

class DeletePassByIdMapperTest {
    @Test
    fun `successful response creation should return successful response`() {
        // GIVEN // WHEN
        val response = DeletePassByIdMapper.successDeletePassByIdResponse()

        // THEN
        assertTrue(response.hasSuccess(), "Must have success")
        assertFalse(response.hasFailure(), "Must not have failure")
    }

    @Test
    fun `response creation via exception with message should return failure response with not empty message`() {
        // GIVEN
        val message = "Some failure message"
        val throwable = RuntimeException(message)

        // WHEN
        val response = DeletePassByIdMapper.failureDeletePassByIdResponse(throwable)

        // THEN
        assertTrue(response.hasFailure(), "Must not have success")
        assertFalse(response.hasSuccess(), "Must have failure")
        assertEquals(message, response.failure.message)
    }

    @Test
    fun `response creation via exception with empty message should return failure response with empty message`() {
        // GIVEN
        val throwable = RuntimeException()

        // WHEN
        val response = DeletePassByIdMapper.failureDeletePassByIdResponse(throwable)

        // THEN
        assertTrue(response.hasFailure(), "Must not have success")
        assertFalse(response.hasSuccess(), "Must have failure")
        assertThat(response.failure.message).isEmpty()
    }
}
