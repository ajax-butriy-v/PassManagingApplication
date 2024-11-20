package com.example.passmanagersvc.passowner.application.service

import com.example.passmanagersvc.passowner.application.port.input.PassOwnerServiceInPort
import com.example.passmanagersvc.passowner.application.port.output.PassOwnerRepositoryOutPort
import com.example.passmanagersvc.passowner.domain.PriceDistribution
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
    private lateinit var passOwnerServiceInPort: PassOwnerServiceInPort

    @MockK
    private lateinit var passOwnerRepositoryOutPort: PassOwnerRepositoryOutPort

    @InjectMockKs
    private lateinit var passOwnerStatisticsService: PassOwnerStatisticsService

    @Test
    fun `calculating spent after date should return positive BigDecimal value if there are passes`() {
        // GIVEN
        every { passOwnerServiceInPort.getById(any()) } returns passOwnerFromDb.toMono()
        every { passOwnerRepositoryOutPort.sumPurchasedAtAfterDate(any(), any()) } returns BigDecimal.valueOf(30)
            .toMono()

        // WHEN
        val spent = passOwnerStatisticsService.calculateSpentAfterDate(LocalDate.now(), passOwnerIdFromDb)

        // THEN
        spent.test()
            .expectNext(BigDecimal.valueOf(30))
            .verifyComplete()

        verify {
            passOwnerServiceInPort.getById(any())
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
