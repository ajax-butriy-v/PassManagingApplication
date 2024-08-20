package com.example.pass_manager

import ClientFixture
import PassFixture
import com.example.pass_manager.exception.ClientAlreadyExistsException
import com.example.pass_manager.repositories.ClientRepository
import com.example.pass_manager.repositories.PassRepository
import com.example.pass_manager.service.ClientServiceImpl
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import io.mockk.verifyOrder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.time.Instant
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class ClientServiceTest {
    @MockK
    private lateinit var clientRepository: ClientRepository

    @MockK
    private lateinit var passRepository: PassRepository

    @InjectMockKs
    private lateinit var clientService: ClientServiceImpl

    private val clientFixture = ClientFixture
    private val passFixture = PassFixture


    @Test
    fun `creation with unique email and phone number should save object`() {
        every { clientRepository.existsByEmailOrPhoneNumber(any(), any()) } returns false
        every { clientRepository.insert(clientFixture.clientToCreate) } returns clientFixture.clientFromDb

        assertThat(clientService.create(clientFixture.clientToCreate)).isEqualTo(clientFixture.clientFromDb)

        verifyOrder {
            clientRepository.existsByEmailOrPhoneNumber(any(), any())
            clientRepository.insert(clientFixture.clientToCreate)
        }
    }

    @Test
    fun `creation with non-unique email and phone number should throw exception`() {
        every { clientRepository.existsByEmailOrPhoneNumber(any(), any()) } returns true

        assertThrows<ClientAlreadyExistsException> { clientService.create(clientFixture.clientToCreate) }

        verify { clientRepository.existsByEmailOrPhoneNumber(any(), any()) }
    }

    @Test
    fun `partial update with unique values should update object`() {
        every { clientRepository.findByEmailAndPhoneNumber(any(), any()) } returns null
        every { clientRepository.save(any()) } returns clientFixture.clientFromDb.copy(firstName = "Changed")

        val updated =
            clientService.update(clientFixture.clientId, clientFixture.clientFromDb.copy(firstName = "Changed"))
        assertThat(updated.firstName).isEqualTo("Changed")

        verify {
            clientRepository.findByEmailAndPhoneNumber(any(), any())
            clientRepository.save(any())
        }
    }

    @Test
    fun `partial update with existing email or phone should throw exception`() {
        every { clientRepository.findByEmailAndPhoneNumber(any(), any()) } returns clientFixture.clientFromDb

        val changedFirstName = clientFixture.clientFromDb.copy(firstName = "Changed")

        assertThrows<ClientAlreadyExistsException> { clientService.update(clientFixture.clientId, changedFirstName) }

        verify { clientRepository.findByEmailAndPhoneNumber(any(), any()) }
    }

    @Test
    fun `cancel pass should delete pass from client list if client id and pass is are valid`() {
        every { clientService.findById(any()) } returns clientFixture.clientFromDb
        justRun { passRepository.deleteById(any()) }

        assertTrue { clientService.cancelPass(clientFixture.clientId, passFixture.singlePassId) }

        verify { passRepository.deleteById(any()) }
    }

    @Test
    fun `calculating spent after date should return positive BigDecimal value if there are passes`() {
        every { clientService.findById(any()) } returns clientFixture.clientFromDb
        every {
            passRepository.findAllByClientAndPurchasedAtAfter(
                any(), any()
            )
        } returns clientFixture.clientFromDb.ownedPasses!!

        assertThat(
            clientService.calculateSpentAfterDate(
                Instant.now(), clientFixture.clientId
            )
        ).isEqualTo(BigDecimal.TEN)

        verify {
            clientService.findById(any())
            passRepository.findAllByClientAndPurchasedAtAfter(any(), any())
        }
    }


}
