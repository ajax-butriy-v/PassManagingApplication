package com.example.gateway.web

import com.example.core.exception.PassNotFoundException
import com.example.core.exception.PassOwnerNotFoundException
import com.example.core.exception.PassTypeNotFoundException
import com.example.gateway.exception.InvalidObjectIdFormatException
import com.example.gateway.util.PassDtoFixture.passDto
import com.example.gateway.util.PassDtoFixture.passDtoWithInvalidIdFormats
import com.example.gateway.util.PassDtoFixture.passId
import com.example.gateway.web.ResponseEntityControllerAdvice.handleBadRequest
import com.example.gateway.web.ResponseEntityControllerAdvice.handleNotFound
import com.example.gateway.web.rest.PassController
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDate
import kotlin.test.BeforeTest
import kotlin.test.Test

@AutoConfigureMockMvc
@ExtendWith(MockKExtension::class)
internal class ResponseEntityControllerAdviceTest {
    private lateinit var mockMvc: MockMvc

    @MockK
    private lateinit var passController: PassController

    private val objectMapper = ObjectMapper()

    @BeforeTest
    fun setupControllerAdvice() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(passController)
            .setControllerAdvice(ResponseEntityControllerAdvice)
            .build()
    }

    @Test
    fun `invalid id formats in dto param should result in handled bad request exception`() {
        val idViolations = listOf(passDtoWithInvalidIdFormats.passTypeId, passDtoWithInvalidIdFormats.passOwnerId)

        // GIVEN
        every { passController.create(any()) } throws InvalidObjectIdFormatException(idViolations)

        // WHEN
        val json = objectMapper.writeValueAsString(passDtoWithInvalidIdFormats)
        val resultActions = mockMvc.perform(
            post("/{url}", PASSES_URL).contentType(MediaType.APPLICATION_JSON).content(json)
        )

        // THEN
        resultActions.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message", `is`("Invalid id formats: [not valid, not valid]")))
    }

    @Test
    fun `non-existing pass by id value should result in handled not found custom exception`() {
        val nonExistingPassId = ObjectId.get()

        // GIVEN
        every {
            passController.transferPass(
                any(),
                any()
            )
        } throws PassNotFoundException(
            PASS_NOT_FOUND_MESSAGE(nonExistingPassId)
        )

        // WHEN
        val resultActions =
            mockMvc.perform(post("/{url}/$nonExistingPassId/transfer/${passDto.passOwnerId}", PASSES_URL))

        // THEN
        resultActions.andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message", `is`(PASS_NOT_FOUND_MESSAGE(nonExistingPassId))))
    }

    @Test
    fun `non-existing pass owner by id value should result in handled not found custom exception`() {
        val nonExistingOwnerId = ObjectId.get()

        // GIVEN
        every { passController.cancelPass(any(), any()) } throws PassOwnerNotFoundException(
            PASS_OWNER_NOT_FOUND_MESSAGE(nonExistingOwnerId)
        )

        // WHEN
        val resultActions = mockMvc.perform(
            post("/{url}/{id}/cancel/{owner-id}", PASSES_URL, passId, nonExistingOwnerId)
                .param("afterDate", LocalDate.now().toString())
        )

        // THEN
        resultActions.andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message", `is`(PASS_OWNER_NOT_FOUND_MESSAGE(nonExistingOwnerId))))
    }

    @Test
    fun `non-existing pass type by id value should result in handled not found custom exception`() {
        val nonExistingPassTypeId = ObjectId.get()

        // GIVEN
        every { passController.create(any()) } throws PassTypeNotFoundException(
            PASS_TYPE_NOT_FOUND_MESSAGE(nonExistingPassTypeId)
        )

        // WHEN
        val json = objectMapper.writeValueAsString(passDtoWithInvalidIdFormats)
        val resultActions = mockMvc.perform(
            post("/{url}", PASSES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )

        // THEN
        resultActions.andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message", `is`(PASS_TYPE_NOT_FOUND_MESSAGE(nonExistingPassTypeId))))
    }

    @Test
    fun `handling not found exceptions returns empty message if exception message is null`() {
        val handleResult = handleNotFound(RuntimeException())
        assertThat(handleResult.body?.message).isEmpty()
    }

    @Test
    fun `handling bad request exceptions returns empty message if exception message is null`() {
        val handleResult = handleBadRequest(RuntimeException())
        assertThat(handleResult.body?.message).isEmpty()
    }

    companion object {
        private const val PASSES_URL = "passes"
        private val PASS_NOT_FOUND_MESSAGE: (ObjectId) -> String = { id -> "Could not find pass by id $id" }
        private val PASS_OWNER_NOT_FOUND_MESSAGE: (ObjectId) -> String = { id -> "Could not find pass owner by id $id" }
        private val PASS_TYPE_NOT_FOUND_MESSAGE: (ObjectId) -> String = { id -> "Could not find pass type by id $id" }
    }
}
