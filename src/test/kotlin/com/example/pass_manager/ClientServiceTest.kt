package com.example.pass_manager

import com.example.pass_manager.data.TestDataFixture
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
import org.junit.jupiter.api.BeforeAll
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

    companion object {
        private lateinit var data: TestDataFixture

        @JvmStatic
        @BeforeAll
        fun setupFixture() {
            data = TestDataFixture()
        }
    }


    @Test
    fun `creation with unique email and phone number should save object`() {
        every { clientRepository.existsByEmailOrPhoneNumber(any(), any()) } returns false
        every { clientRepository.insert(data.clientToCreate) } returns data.clientFromDb

        assertThat(clientService.create(data.clientToCreate)).isEqualTo(data.clientFromDb)

        verifyOrder {
            clientRepository.existsByEmailOrPhoneNumber(any(), any())
            clientRepository.insert(data.clientToCreate)
        }
    }

    @Test
    fun `creation with non-unique email and phone number should throw exception`() {
        every { clientRepository.existsByEmailOrPhoneNumber(any(), any()) } returns true

        assertThrows<ClientAlreadyExistsException> { clientService.create(data.clientToCreate) }

        verify { clientRepository.existsByEmailOrPhoneNumber(any(), any()) }
    }

    @Test
    fun `partial update with unique values should update object`() {
        every { clientRepository.findByEmailAndPhoneNumber(any(), any()) } returns null
        every { clientRepository.save(any()) } returns data.clientFromDb.copy(firstName = "Changed")

        val updated = clientService.update(data.clientId, data.clientFromDb.copy(firstName = "Changed"))
        assertThat(updated.firstName).isEqualTo("Changed")

        verify {
            clientRepository.findByEmailAndPhoneNumber(any(), any())
            clientRepository.save(any())
        }
    }

    @Test
    fun `partial update with existing email or phone should throw exception`() {
        every { clientRepository.findByEmailAndPhoneNumber(any(), any()) } returns data.clientFromDb

        val changedFirstName = data.clientFromDb.copy(firstName = "Changed")

        assertThrows<ClientAlreadyExistsException> { clientService.update(data.clientId, changedFirstName) }

        verify { clientRepository.findByEmailAndPhoneNumber(any(), any()) }
    }

    @Test
    fun `cancel pass should delete pass from client list if client id and pass is are valid`() {
        every { clientService.findById(any()) } returns data.clientFromDb
        justRun { passRepository.deleteById(any()) }

        assertTrue { clientService.cancelPass(data.clientId, data.passId) }

        verify { passRepository.deleteById(any()) }
    }

    @Test
    fun `calculating spent after date should return positive BigDecimal value if there are passes`() {
        every { clientService.findById(any()) } returns data.clientFromDb
        every {
            passRepository.findAllByClientAndPurchasedAtAfter(
                any(),
                any()
            )
        } returns data.clientFromDb.ownedPasses!!

        assertThat(clientService.calculateSpentAfterDate(Instant.now(), data.clientId)).isEqualTo(BigDecimal.TEN)

        verify {
            clientService.findById(any())
            passRepository.findAllByClientAndPurchasedAtAfter(any(), any())
        }
    }


}
