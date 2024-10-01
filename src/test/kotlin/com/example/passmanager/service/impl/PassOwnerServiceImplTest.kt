package com.example.passmanager.service.impl

import com.example.passmanager.domain.MongoPassOwner
import com.example.passmanager.exception.PassOwnerNotFoundException
import com.example.passmanager.repositories.PassOwnerRepository
import com.example.passmanager.repositories.PassRepository
import com.example.passmanager.util.PassOwnerFixture.passOwnerFromDb
import com.example.passmanager.util.PassOwnerFixture.passOwnerIdFromDb
import com.example.passmanager.util.PassOwnerFixture.passOwnerToCreate
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test
import reactor.kotlin.test.verifyError

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
        // GIVEN
        every { passOwnerRepository.insert(any<MongoPassOwner>()) } returns passOwnerFromDb.toMono()

        // WHEN
        val created = passOwnerService.create(passOwnerToCreate)

        // THEN
        created.test()
            .assertNext { assertThat(it).usingRecursiveAssertion().isEqualTo(passOwnerFromDb) }
            .verifyComplete()

        verify { passOwnerRepository.insert(any<MongoPassOwner>()) }
    }

    @Test
    fun `partial update with unique values should update object`() {
        // GIVEN
        every { passOwnerRepository.save(any()) } returns passOwnerFromDb.copy(firstName = "Changed").toMono()

        // WHEN
        val updated = passOwnerService.update(passOwnerFromDb.copy(firstName = "Changed"))

        // THEN
        updated.test()
            .assertNext { assertThat(it.firstName).isEqualTo("Changed") }
            .verifyComplete()

        verify { passOwnerRepository.save(any()) }
    }

    @Test
    fun `find by id should return object with specified id`() {
        // GIVEN
        every { passOwnerRepository.findById(any()) } returns passOwnerFromDb.toMono()

        // WHEN
        val ownerById = passOwnerService.findById(passOwnerIdFromDb)

        // THEN
        ownerById.test()
            .assertNext { assertThat(it).usingRecursiveAssertion().isEqualTo(passOwnerFromDb) }
            .verifyComplete()

        verify { passOwnerRepository.findById(any()) }
    }

    @Test
    fun `delete by id should delete object`() {
        // GIVEN
        every { passOwnerRepository.deleteById(any()) } returns Unit.toMono()
        every { passRepository.deleteAllByOwnerId(any()) } returns Unit.toMono()

        // WHEN
        val delete = passOwnerService.deleteById(passOwnerIdFromDb)

        // THEN
        delete.test()
            .expectNext(Unit)
            .verifyComplete()

        verify {
            passOwnerRepository.deleteById(any())
            passRepository.deleteAllByOwnerId(any())
        }
    }

    @Test
    fun `get by id should return value if object is present in db`() {
        // GIVEN
        every { passOwnerRepository.findById(any()) } returns passOwnerFromDb.toMono()

        // WHEN
        val ownerById = passOwnerService.getById(passOwnerIdFromDb)

        // THEN
        ownerById.test()
            .expectNext(passOwnerFromDb)
            .verifyComplete()

        verify { passOwnerRepository.findById(any()) }
    }

    @Test
    fun `get by id should return throw error if object is not present in db`() {
        // GIVEN
        every { passOwnerRepository.findById(any()) } returns Mono.empty()

        // WHEN
        val ownerById = passOwnerService.getById(passOwnerIdFromDb)

        // THEN
        ownerById.test().verifyError<PassOwnerNotFoundException>()

        verify { passOwnerRepository.findById(any()) }
    }
}
