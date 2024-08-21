package com.example.pass_manager.service


import com.example.pass_manager.domain.MongoPassOwner
import com.example.pass_manager.repositories.PassOwnerRepository
import com.example.pass_manager.service.impl.PassOwnerServiceImpl
import com.example.pass_manager.util.PassOwnerFixture.passOwnerFromDb
import com.example.pass_manager.util.PassOwnerFixture.passOwnerId
import com.example.pass_manager.util.PassOwnerFixture.passOwnerToCreate
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull

@ExtendWith(MockKExtension::class)
internal class PassOwnerServiceTest {
    @MockK
    private lateinit var passOwnerRepository: PassOwnerRepository

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
        val updated = passOwnerService.update(passOwnerId, passOwnerFromDb.copy(firstName = "Changed"))
        assertThat(updated.firstName).isEqualTo("Changed")

        // THEN
        verify { passOwnerRepository.save(any()) }
    }

    @Test
    fun `find by id should return object with specified id`() {
        every { passOwnerRepository.findByIdOrNull(any()) } returns passOwnerFromDb

        assertThat(passOwnerService.findById(passOwnerId)).isEqualTo(passOwnerFromDb)

        verify { passOwnerRepository.findByIdOrNull(any()) }
    }

    @Test
    fun `delete by id should delete object`() {
        justRun { passOwnerRepository.deleteById(any()) }

        passOwnerService.deleteById(passOwnerId)

        verify { passOwnerService.deleteById(any()) }
    }
}

