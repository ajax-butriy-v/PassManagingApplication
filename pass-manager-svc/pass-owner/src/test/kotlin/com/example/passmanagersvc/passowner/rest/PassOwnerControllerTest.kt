package com.example.passmanagersvc.passowner.rest

import com.example.passmanagersvc.passowner.application.port.input.PassOwnerServiceInputPort
import com.example.passmanagersvc.passowner.application.port.input.PassOwnerStatisticsServiceInputPort
import com.example.passmanagersvc.passowner.domain.PriceDistribution
import com.example.passmanagersvc.passowner.infrastructure.rest.PassOwnerController
import com.example.passmanagersvc.passowner.infrastructure.rest.dto.PassOwnerDto
import com.example.passmanagersvc.passowner.infrastructure.rest.dto.SpentAfterDateDto
import com.example.passmanagersvc.passowner.infrastructure.rest.mapper.PassOwnerMapper.toDto
import com.example.passmanagersvc.util.PassOwnerFixture.passOwnerFromDb
import com.example.passmanagersvc.util.PassOwnerFixture.passOwnerIdFromDb
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import java.math.BigDecimal
import java.time.LocalDate

@WebFluxTest(PassOwnerController::class)
@TestPropertySource(properties = ["mongock.enabled=false"])
@ActiveProfiles("test")
internal class PassOwnerControllerTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var passOwnerServiceInputPort: PassOwnerServiceInputPort

    @MockkBean
    private lateinit var passOwnerStatisticsServiceInputPort: PassOwnerStatisticsServiceInputPort

    @Test
    fun `calculating price distributions should return valid price distributions`() {
        // GIVEN
        val priceDistributions = listOf("First type", "Second type", "Third type")
            .map { PriceDistribution(it, BigDecimal.TEN) }
        every {
            passOwnerStatisticsServiceInputPort.calculatePriceDistributions(any())
        } returns priceDistributions.toFlux()

        // WHEN // THEN
        webTestClient.get()
            .uri("$URL/$passOwnerIdFromDb/distributions")
            .exchange()
            .expectStatus().isOk
            .expectBody<List<PriceDistribution>>()
            .isEqualTo(priceDistributions)
    }

    @Test
    fun `calculating spent after date should return valid sum`() {
        // GIVEN
        val sum = BigDecimal.valueOf(100)
        val afterDate = LocalDate.now()
        every { passOwnerStatisticsServiceInputPort.calculateSpentAfterDate(any(), any()) } returns sum.toMono()

        // WHEN // THEN
        webTestClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("$URL/$passOwnerIdFromDb/spent")
                    .queryParam("afterDate", afterDate)
                    .build()
            }
            .exchange()
            .expectStatus().isOk
            .expectBody<SpentAfterDateDto>()
            .isEqualTo(SpentAfterDateDto(afterDate = afterDate, passOwnerId = passOwnerIdFromDb, total = sum))
    }

    @Test
    fun `updating pass owner should return partially updated object`() {
        // GIVEN
        val partiallyUpdated = passOwnerFromDb.copy(firstName = "updated")
        every { passOwnerServiceInputPort.getById(any()) } returns passOwnerFromDb.toMono()
        every { passOwnerServiceInputPort.update(any(), any()) } returns partiallyUpdated.toMono()

        // WHEN // THEN
        webTestClient.patch()
            .uri("$URL/$passOwnerIdFromDb")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(partiallyUpdated.toDto())
            .exchange()
            .expectStatus().isOk
            .expectBody<PassOwnerDto>()
            .isEqualTo(partiallyUpdated.toDto())
    }

    @Test
    fun `creating pass owner should create a new pass owner`() {
        // GIVEN
        every { passOwnerServiceInputPort.create(any()) } returns passOwnerFromDb.toMono()
        val dto = passOwnerFromDb.toDto()

        // WHEN // THEN
        webTestClient.post()
            .uri(URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(dto)
            .exchange()
            .expectStatus().isCreated
            .expectBody<PassOwnerDto>()
            .isEqualTo(dto)
    }

    @Test
    fun `deleting pass owner by id should delete pass owner by id`() {
        // GIVEN
        every { passOwnerServiceInputPort.deleteById(any()) } returns Unit.toMono()

        // WHEN // THEN
        webTestClient.delete()
            .uri("$URL/$passOwnerIdFromDb")
            .exchange()
            .expectStatus().isNoContent
    }

    private companion object {
        const val URL = "/owners"
    }
}
