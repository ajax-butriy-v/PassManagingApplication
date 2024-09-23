package com.example.passmanager.service.impl

import com.example.passmanager.domain.MongoPassOwner
import com.example.passmanager.repositories.PassOwnerRepository
import com.example.passmanager.repositories.PassRepository
import com.example.passmanager.util.PassOwnerFixture.passOwnerFromDb
import com.example.passmanager.util.PassOwnerFixture.passOwnerIdFromDb
import com.example.passmanager.util.PassOwnerFixture.passOwnerToCreate
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class PassOwnerServiceImplTest {
    @MockK
    private lateinit var passOwnerRepository: PassOwnerRepository

    @MockK
    private lateinit var passRepository: PassRepository

    @InjectMockKs
    private lateinit var passOwnerService: PassOwnerServiceImpl

    @Test
    fun `creation should return new object with id`() {
        every { passOwnerRepository.insert(any<MongoPassOwner>()) } returns passOwnerFromDb

        assertThat(passOwnerService.create(passOwnerToCreate)).isEqualTo(passOwnerFromDb)

        verify { passOwnerRepository.insert(any<MongoPassOwner>()) }
    }

    @Test
    fun `partial update with unique values should update object`() {
        // GIVEN
        every { passOwnerRepository.save(any()) } returns passOwnerFromDb.copy(firstName = "Changed")

        // WHEN
        val updated = passOwnerService.update(passOwnerFromDb.copy(firstName = "Changed"))
        assertThat(updated.firstName).isEqualTo("Changed")

        // THEN
        verify { passOwnerRepository.save(any()) }
    }

    @Test
    fun `find by id should return object with specified id`() {
        every { passOwnerRepository.findById(any()) } returns passOwnerFromDb

        assertThat(passOwnerService.findById(passOwnerIdFromDb)).isEqualTo(passOwnerFromDb)

        verify { passOwnerRepository.findById(any()) }
    }

    @Test
    fun `delete by id should delete object`() {
        justRun { passOwnerRepository.deleteById(any()) }
        justRun { passRepository.deleteAllByOwnerId(any()) }

        passOwnerService.deleteById(passOwnerIdFromDb)

        verify {
            passOwnerRepository.deleteById(any())
            passRepository.deleteAllByOwnerId(any())
        }
    }
}
