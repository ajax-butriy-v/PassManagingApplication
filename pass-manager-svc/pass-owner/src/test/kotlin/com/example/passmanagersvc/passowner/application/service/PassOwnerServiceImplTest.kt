package com.example.passmanagersvc.passowner.application.service

import com.example.core.exception.PassOwnerNotFoundException
import com.example.passmanagersvc.passowner.application.port.output.PassOwnerRepositoryOutPort
import com.example.passmanagersvc.passowner.domain.PassOwner
import com.example.passmanagersvc.util.PassOwnerFixture.passOwnerFromDb
import com.example.passmanagersvc.util.PassOwnerFixture.passOwnerIdFromDb
import com.example.passmanagersvc.util.PassOwnerFixture.passOwnerToCreate
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
    private lateinit var passOwnerRepositoryOutPort: PassOwnerRepositoryOutPort

    @InjectMockKs
    private lateinit var passOwnerService: PassOwnerService

    @Test
    fun `creation should return new object with id`() {
        // GIVEN
        every { passOwnerRepositoryOutPort.insert(any<PassOwner>()) } returns passOwnerFromDb.toMono()

        // WHEN
        val created = passOwnerService.create(passOwnerToCreate)

        // THEN
        created.test()
            .assertNext { assertThat(it).usingRecursiveAssertion().isEqualTo(passOwnerFromDb) }
            .verifyComplete()

        verify { passOwnerRepositoryOutPort.insert(any<PassOwner>()) }
    }

    @Test
    fun `partial update with unique values should update object`() {
        // GIVEN
        val updatedPassOwner = passOwnerFromDb.copy(firstName = "changed")
        every { passOwnerRepositoryOutPort.save(any()) } returns updatedPassOwner.toMono()

        // WHEN
        val updated = passOwnerService.update(passOwnerIdFromDb, updatedPassOwner)

        // THEN
        updated.test()
            .assertNext { assertThat(it.firstName).isEqualTo("changed") }
            .verifyComplete()

        verify {
            passOwnerRepositoryOutPort.save(any())
        }
    }

    @Test
    fun `delete by id should delete object`() {
        // GIVEN
        every { passOwnerRepositoryOutPort.deleteById(any()) } returns Unit.toMono()

        // WHEN
        val delete = passOwnerService.deleteById(passOwnerIdFromDb)

        // THEN
        delete.test()
            .expectNext(Unit)
            .verifyComplete()

        verify {
            passOwnerRepositoryOutPort.deleteById(any())
        }
    }

    @Test
    fun `get by id should return value if object is present in db`() {
        // GIVEN
        every { passOwnerRepositoryOutPort.findById(any()) } returns passOwnerFromDb.toMono()

        // WHEN
        val ownerById = passOwnerService.getById(passOwnerIdFromDb)

        // THEN
        ownerById.test()
            .expectNext(passOwnerFromDb)
            .verifyComplete()

        verify { passOwnerRepositoryOutPort.findById(any()) }
    }

    @Test
    fun `get by id should return throw error if object is not present in db`() {
        // GIVEN
        every { passOwnerRepositoryOutPort.findById(any()) } returns Mono.empty()

        // WHEN
        val ownerById = passOwnerService.getById(passOwnerIdFromDb)

        // THEN
        ownerById.test().verifyError<PassOwnerNotFoundException>()

        verify { passOwnerRepositoryOutPort.findById(any()) }
    }
}
