package com.example.passmanagersvc.service.impl

import com.example.passmanagersvc.repositories.PassRepository
import com.example.passmanagersvc.service.PassOwnerService
import com.example.passmanagersvc.util.PassOwnerFixture.passOwnerFromDb
import com.example.passmanagersvc.util.PassOwnerFixture.passOwnerIdFromDb
import com.example.passmanagersvc.web.dto.PriceDistribution
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
    private lateinit var passOwnerService: PassOwnerService

    @MockK
    private lateinit var passRepository: PassRepository

    @InjectMockKs
    private lateinit var passOwnerStatisticsService: PassOwnerStatisticsServiceImpl

    @Test
    fun `calculating spent after date should return positive BigDecimal value if there are passes`() {
        // GIVEN
        every { passOwnerService.getById(any()) } returns passOwnerFromDb.toMono()
        every { passRepository.sumPurchasedAtAfterDate(any(), any()) } returns BigDecimal.valueOf(30).toMono()

        // WHEN
        val spent = passOwnerStatisticsService.calculateSpentAfterDate(LocalDate.now(), passOwnerIdFromDb)

        // THEN
        spent.test()
            .expectNext(BigDecimal.valueOf(30))
            .verifyComplete()

        verify {
            passOwnerService.getById(any())
            passRepository.sumPurchasedAtAfterDate(any(), any())
        }
    }

    @Test
    fun `calculating price distribution for client should return valid result list`() {
        // GIVEN
        val priceDistributions = listOf("First type", "Second type", "Third type")
            .map { PriceDistribution(it, BigDecimal.TEN) }
        every { passRepository.getPassesPriceDistribution(any()) } returns priceDistributions.toFlux()

        // WHEN
        val actual = passOwnerStatisticsService.calculatePriceDistributions(passOwnerIdFromDb)

        // THEN
        actual.collectList()
            .test()
            .expectNext(priceDistributions)
            .verifyComplete()

        verify { passRepository.getPassesPriceDistribution(any()) }
    }
}
