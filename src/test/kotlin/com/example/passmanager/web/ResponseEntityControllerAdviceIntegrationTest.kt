package com.example.passmanager.web

import com.example.passmanager.service.PassOwnerService
import com.example.passmanager.util.PassFixture.dtoWithInvalidIdFormats
import com.example.passmanager.util.PassFixture.dtoWithValidIdFormats
import com.example.passmanager.util.PassFixture.singlePassId
import com.example.passmanager.util.PassOwnerFixture.passOwnerFromDb
import com.example.passmanager.util.PassOwnerFixture.passOwnerId
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.`is`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import kotlin.test.Test

@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
internal class ResponseEntityControllerAdviceIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var passOwnerService: PassOwnerService

    private val objectMapper = ObjectMapper()

    @Test
    fun `invalid id formats in dto param should result in handled bad request exception`() {
        val json = objectMapper.writeValueAsString(dtoWithInvalidIdFormats)
        val resultActions = mockMvc.perform(
            post("/{url}", PASSES_URL).contentType(MediaType.APPLICATION_JSON).content(json)
        )

        resultActions.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message", `is`("Invalid id formats: [not valid, not valid]")))
    }

    @Test
    fun `non-existing pass by id value should result in handled not found custom exception`() {
        mockMvc.perform(post("/{url}/$singlePassId/transfer/$passOwnerId", PASSES_URL))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message", `is`("Could not find pass by id $singlePassId")))
    }


    @Test
    fun `non-existing pass owner by id value should result in handled not found custom exception`() {
        val nonExistingOwnerId = dtoWithValidIdFormats.passOwnerId
        mockMvc.perform(
            get("/{url}/{id}/spent", OWNERS_URL, nonExistingOwnerId)
                .param("afterDate", Instant.now().toString())
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message", `is`("Could not find pass owner by id $nonExistingOwnerId")))
    }

    @Test
    fun `non-existing pass type by id value should result in handled not found custom exception`() {
        passOwnerService.create(passOwnerFromDb)

        val json = objectMapper.writeValueAsString(dtoWithValidIdFormats)
        val resultActions = mockMvc.perform(
            post("/{url}", PASSES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )

        val nonExistingPassTypeId = dtoWithValidIdFormats.passTypeId
        resultActions.andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message", `is`("Could not find pass type by id $nonExistingPassTypeId")))

    }

    companion object {
        private const val PASSES_URL = "passes"
        private const val OWNERS_URL = "owners"
    }
}
