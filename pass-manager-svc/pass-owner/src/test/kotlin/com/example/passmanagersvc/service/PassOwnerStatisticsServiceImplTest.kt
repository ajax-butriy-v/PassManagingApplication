package com.example.passmanagersvc.service

import com.example.passmanagersvc.application.port.input.PassOwnerServiceInputPort
import com.example.passmanagersvc.application.port.out.PassOwnerRepositoryOutPort
import com.example.passmanagersvc.application.port.service.PassOwnerStatisticsService
import com.example.passmanagersvc.domain.PriceDistribution
import com.example.passmanagersvc.util.PassOwnerFixture.passOwnerFromDb
import com.example.passmanagersvc.util.PassOwnerFixture.passOwnerIdFromDb
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.extension.ExtendWith
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
internal class PassOwnerStatisticsServiceImplTest {
    @MockK
    private lateinit var passOwnerServiceInputPort: PassOwnerServiceInputPort

    @MockK
    private lateinit var passOwnerRepositoryOutPort: PassOwnerRepositoryOutPort

    @InjectMockKs
    private lateinit var passOwnerStatisticsService: PassOwnerStatisticsService

    @Test
    fun `calculating spent after date should return positive BigDecimal value if there are passes`() {
        // GIVEN
        every { passOwnerServiceInputPort.getById(any()) } returns passOwnerFromDb.toMono()
        every { passOwnerRepositoryOutPort.sumPurchasedAtAfterDate(any(), any()) } returns BigDecimal.valueOf(30)
            .toMono()

        // WHEN
        val spent = passOwnerStatisticsService.calculateSpentAfterDate(LocalDate.now(), passOwnerIdFromDb)

        // THEN
        spent.test()
            .expectNext(BigDecimal.valueOf(30))
            .verifyComplete()

        verify {
            passOwnerServiceInputPort.getById(any())
            passOwnerRepositoryOutPort.sumPurchasedAtAfterDate(any(), any())
        }
    }

    @Test
    fun `calculating price distribution for client should return valid result list`() {
        // GIVEN
        val priceDistributions = listOf("First type", "Second type", "Third type")
            .map { PriceDistribution(it, BigDecimal.TEN) }
        every { passOwnerRepositoryOutPort.getPassesPriceDistribution(any()) } returns priceDistributions.toFlux()

        // WHEN
        val actual = passOwnerStatisticsService.calculatePriceDistributions(passOwnerIdFromDb)

        // THEN
        actual.collectList()
            .test()
            .expectNext(priceDistributions)
            .verifyComplete()

        verify { passOwnerRepositoryOutPort.getPassesPriceDistribution(any()) }
    }
}
