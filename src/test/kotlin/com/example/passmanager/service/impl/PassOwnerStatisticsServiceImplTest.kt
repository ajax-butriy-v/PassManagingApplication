package com.example.passmanager.service.impl

import com.example.passmanager.service.PassOwnerService
import com.example.passmanager.service.PassService
import com.example.passmanager.util.PassFixture.passes
import com.example.passmanager.util.PassOwnerFixture
import com.example.passmanager.util.PassOwnerFixture.passOwnerId
import com.example.passmanager.web.dto.PriceDistribution
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.time.Instant
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
internal class PassOwnerStatisticsServiceImplTest {
    @MockK
    private lateinit var passOwnerService: PassOwnerService

    @MockK
    private lateinit var passService: PassService

    @InjectMockKs
    private lateinit var passOwnerStatisticsService: PassOwnerStatisticsServiceImpl

    @Test
    fun `calculating spent after date should return positive BigDecimal value if there are passes`() {
        // GIVEN
        every { passOwnerService.findById(any()) } returns PassOwnerFixture.passOwnerFromDb
        every { passService.findAllByPassOwnerAndPurchasedAtGreaterThan(any(), any()) } returns passes

        // WHEN
        assertThat(passOwnerStatisticsService.calculateSpentAfterDate(Instant.now(), passOwnerId))
            .isEqualTo(BigDecimal.valueOf(30))

        // THEN
        verify {
            passOwnerService.findById(any())
            passService.findAllByPassOwnerAndPurchasedAtGreaterThan(any(), any())
        }
    }

    @Test
    fun `calculating price distribution for client should return valid result list`() {
        // GIVEN
        every { passService.findAllByPassOwnerId(any()) } returns passes

        // WHEN
        val priceDistributions = passOwnerStatisticsService.calculatePriceDistributions(passOwnerId)
        val expected = listOf("First type", "Second type", "Third type").map { PriceDistribution(it, BigDecimal.TEN) }
        assertThat(priceDistributions).isEqualTo(expected)

        // THEN
        verify { passService.findAllByPassOwnerId(any()) }
    }
}

