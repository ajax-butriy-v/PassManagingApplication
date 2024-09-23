package com.example.passmanager.service.impl

import com.example.passmanager.repositories.PassRepository
import com.example.passmanager.service.PassOwnerService
import com.example.passmanager.util.PassOwnerFixture
import com.example.passmanager.util.PassOwnerFixture.passOwnerIdFromDb
import com.example.passmanager.web.dto.PriceDistribution
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith
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
        every { passOwnerService.getById(any()) } returns PassOwnerFixture.passOwnerFromDb
        every { passRepository.sumPurchasedAtAfterDate(any(), any()) } returns BigDecimal.valueOf(30)

        // WHEN
        assertThat(passOwnerStatisticsService.calculateSpentAfterDate(LocalDate.now(), passOwnerIdFromDb))
            .isEqualTo(BigDecimal.valueOf(30))

        // THEN
        verify {
            passOwnerService.getById(any())
            passRepository.sumPurchasedAtAfterDate(any(), any())
        }
    }

    @Test
    fun `calculating price distribution for client should return valid result list`() {
        val priceDistributions = listOf("First type", "Second type", "Third type")
            .map { PriceDistribution(it, BigDecimal.TEN) }
        // GIVEN
        every { passRepository.getPassesPriceDistribution(any()) } returns priceDistributions

        // WHEN
        val actual = passOwnerStatisticsService.calculatePriceDistributions(passOwnerIdFromDb)
        assertThat(actual).isEqualTo(priceDistributions)

        // THEN
        verify { passRepository.getPassesPriceDistribution(any()) }
    }
}
