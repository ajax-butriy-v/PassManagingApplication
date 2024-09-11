package com.example.passmanager.web

import com.example.passmanager.web.ResponseEntityControllerAdvice.handleBadRequest
import com.example.passmanager.web.ResponseEntityControllerAdvice.handleNotFound
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ResponseEntityControllerAdviceTest {
    @Test
    fun `handling not found exceptions returns mpty message if exception message is null`() {
        val handleResult = handleNotFound(RuntimeException())
        assertThat(handleResult.body?.message).isEmpty()
    }

    @Test
    fun `handling bad request exceptions returns mpty message if exception message is null`() {
        val handleResult = handleBadRequest(RuntimeException())
        assertThat(handleResult.body?.message).isEmpty()
    }
}
