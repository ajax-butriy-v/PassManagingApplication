package com.example.pass_manager

import ClientFixture
import PassFixture
import com.example.pass_manager.repositories.PassRepository
import com.example.pass_manager.service.ClientService
import com.example.pass_manager.service.PassServiceImpl
import com.example.pass_manager.web.dto.PriceDistribution
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import io.mockk.verifyOrder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
class PassServiceTest {
    @MockK
    private lateinit var passRepository: PassRepository

    @MockK
    private lateinit var clientService: ClientService

    @InjectMockKs
    private lateinit var passService: PassServiceImpl

    private val clientFixture = ClientFixture
    private val passFixture = PassFixture

    @Test
    fun `calculating price distribution for client should return valid result list`() {
        every { passRepository.findAllByClientId(any()) } returns passFixture.passes

        val priceDistributions = passService.calculatePriceDistribution(clientFixture.clientId)
        val expected = listOf("First type", "Second type", "Third type").map { PriceDistribution(it, BigDecimal.TEN) }

        assertThat(priceDistributions).isEqualTo(expected)

        verify { passRepository.findAllByClientId(any()) }
    }

    @Test
    fun `transfering pass to another client should complete if all ids are valid`() {
        every { passService.findById(any()) } returns passFixture.singlePass
        every { clientService.findById(any()) } returns clientFixture.clientFromDb
        justRun { passRepository.updateMongoPassByClient(passFixture.singlePass, clientFixture.clientFromDb) }

        assertThat(passService.transferPassToAnotherClient(passFixture.singlePassId, clientFixture.clientId))

        verifyOrder {
            passService.findById(any())
            clientService.findById(any())
            passRepository.updateMongoPassByClient(any(), any())
        }
    }
}
