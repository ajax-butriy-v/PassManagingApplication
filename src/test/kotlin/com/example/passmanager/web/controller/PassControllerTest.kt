package com.example.passmanager.web.controller

import com.example.passmanager.service.PassManagementService
import com.example.passmanager.service.PassService
import com.example.passmanager.util.PassFixture.dtoWithValidIdFormats
import com.example.passmanager.util.PassFixture.passFromDb
import com.example.passmanager.util.PassFixture.singlePassId
import com.example.passmanager.util.PassOwnerFixture.passOwnerIdFromDb
import com.example.passmanager.web.dto.PassDto
import com.example.passmanager.web.mapper.PassMapper.toDto
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import reactor.kotlin.core.publisher.toMono

@ExtendWith(MockKExtension::class)
@WebFluxTest(PassController::class)
@ActiveProfiles("test")
class PassControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var passService: PassService

    @MockkBean
    private lateinit var passManagementService: PassManagementService

    @Test
    fun `find by id should return pass by id`() {
        // GIVEN
        every { passService.findById(any()) } returns passFromDb.toMono()

        // WHEN // THEN
        webTestClient.get()
            .uri("$URL/$singlePassId")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody<PassDto>()
            .isEqualTo(passFromDb.toDto())
    }

    @Test
    fun `creating pass should create a new pass`() {
        // GIVEN
        every { passService.create(any(), any(), any()) } returns passFromDb.toMono()

        // WHEN // THEN
        webTestClient.post()
            .uri(URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(dtoWithValidIdFormats)
            .exchange()
            .expectStatus().isCreated
            .expectBody<PassDto>()
            .isEqualTo(passFromDb.toDto())
    }

    @Test
    fun `canceling a pass should cancel a pass`() {
        // GIVEN
        every { passManagementService.cancelPass(any(), any()) } returns Unit.toMono()

        // WHEN // THEN
        webTestClient.post()
            .uri("$URL/$singlePassId/cancel/$passOwnerIdFromDb")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `transferring a pass should transfer a pass`() {
        // GIVEN
        every { passManagementService.transferPass(any(), any()) } returns Unit.toMono()

        // WHEN // THEN
        webTestClient.post()
            .uri("$URL/$singlePassId/transfer/$passOwnerIdFromDb")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `deleting pass by id should delete pass by id`() {
        // GIVEN
        every { passService.deleteById(any()) } returns Unit.toMono()

        // WHEN // THEN
        webTestClient.delete()
            .uri("$URL/$singlePassId")
            .exchange()
            .expectStatus().isNoContent
    }

    private companion object {
        const val URL = "/passes"
    }
}
