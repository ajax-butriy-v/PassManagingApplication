package com.example.passmanager.service.impl

import com.example.passmanager.exception.PassNotFoundException
import com.example.passmanager.repositories.PassRepository
import com.example.passmanager.service.PassOwnerService
import com.example.passmanager.service.PassTypeService
import com.example.passmanager.util.PassFixture.passFromDb
import com.example.passmanager.util.PassFixture.passToCreate
import com.example.passmanager.util.PassFixture.passesFromDb
import com.example.passmanager.util.PassFixture.singlePassId
import com.example.passmanager.util.PassFixture.singlePassType
import com.example.passmanager.util.PassFixture.singlePassTypeId
import com.example.passmanager.util.PassOwnerFixture.passOwnerFromDb
import com.example.passmanager.util.PassOwnerFixture.passOwnerIdFromDb
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test
import reactor.kotlin.test.verifyError
import java.math.BigDecimal
import java.time.LocalDate

@ExtendWith(MockKExtension::class)
internal class PassServiceImplTest {
    @MockK
    private lateinit var passRepository: PassRepository

    @MockK
    private lateinit var passOwnerService: PassOwnerService

    @MockK
    private lateinit var passTypeService: PassTypeService

    @InjectMockKs
    private lateinit var passService: PassServiceImpl

    @Test
    fun `creation should return new object with id`() {
        // GIVEN
        every { passOwnerService.getById(passOwnerIdFromDb) } returns passOwnerFromDb.toMono()
        every { passTypeService.getById(singlePassTypeId) } returns singlePassType.toMono()
        every { passRepository.insert(any()) } returns passFromDb.toMono()

        // WHEN
        val created = passService.create(passToCreate, passOwnerIdFromDb, singlePassTypeId)

        // THEN
        created.test()
            .expectNext(passFromDb)
            .verifyComplete()

        verify { passRepository.insert(any()) }
    }

    @Test
    fun `find by id should return object with specified id`() {
        // GIVEN
        every { passRepository.findById(any()) } returns passFromDb.toMono()

        // WHEN
        val foundById = passService.findById(singlePassId)

        // THEN
        foundById.test()
            .assertNext { passFromDb }
            .verifyComplete()

        verify { passRepository.findById(any()) }
    }

    @Test
    fun `delete by id should delete object`() {
        // GIVEN
        every { passRepository.deleteById(any()) } returns Unit.toMono()

        // WHEN
        val delete = passService.deleteById(singlePassId)

        // THEN
        delete.test()
            .expectNext(Unit)
            .verifyComplete()
        verify { passService.deleteById(any()) }
    }

    @Test
    fun `should return all passes for owner after particular date`() {
        // GIVEN
        every { passRepository.findByOwnerAndPurchasedAfter(any(), any()) } returns passesFromDb.toFlux()

        // WHEN
        val afterDateList = passService.findAllByPassOwnerAndPurchasedAtGreaterThan(passOwnerIdFromDb, LocalDate.MIN)

        // THEN
        afterDateList.collectList()
            .test()
            .assertNext { assertThat(it).hasSize(3) }
            .verifyComplete()

        verify { passRepository.findByOwnerAndPurchasedAfter(any(), any()) }
    }

    @Test
    fun `finding all passes by pass owner id should return all corresponding passes`() {
        // GIVEN
        every { passRepository.findAllByPassOwnerId(any()) } returns passesFromDb.toFlux()

        // WHEN
        val passesByOwner = passService.findAllByPassOwnerId(passOwnerIdFromDb)

        // THEN
        passesByOwner.collectList()
            .test()
            .assertNext { assertThat(it).hasSize(3) }
            .verifyComplete()

        verify { passRepository.findAllByPassOwnerId(any()) }
    }

    @Test
    fun `updating pass should return updated object`() {
        // GIVEN
        val changedPass = passFromDb.copy(purchasedFor = BigDecimal.valueOf(200))
        every { passRepository.save(any()) } returns changedPass.toMono()

        // WHEN
        val actual = passService.update(changedPass)

        // THEN
        actual.test()
            .expectNext(changedPass)
            .verifyComplete()

        verify { passRepository.save(any()) }
    }

    @Test
    fun `get by id should return pass if it exists`() {
        // GIVEN
        every { passRepository.findById(any()) } returns passFromDb.toMono()

        // WHEN
        val passById = passService.getById(singlePassId)

        // THEN
        passById.test()
            .expectNext(passFromDb)
            .verifyComplete()

        verify { passRepository.findById(any()) }
    }

    @Test
    fun `get by id should throw exception if pass not exist`() {
        // GIVEN
        every { passRepository.findById(any()) } returns Mono.empty()

        // WHEN
        val passById = passService.getById(singlePassId)

        // THEN
        passById.test().verifyError<PassNotFoundException>()
    }

    @Test
    fun `deleting all passes by pass owner id should delete all corresponding passes`() {
        // GIVEN
        every { passRepository.deleteAllByOwnerId(any()) } returns Unit.toMono()

        // WHEN
        val delete = passService.deleteAllByOwnerId(singlePassId)

        // THEN
        delete.test()
            .expectNext(Unit)
            .verifyComplete()

        verify { passRepository.deleteAllByOwnerId(any()) }
    }

    @Test
    fun `finding all by pass owner and purchased at should return corresponding passes`() {
        // GIVEN
        every { passRepository.findByOwnerAndPurchasedAfter(any(), any()) } returns passesFromDb.toFlux()

        // WHEN
        val actual = passService.findAllByPassOwnerAndPurchasedAtGreaterThan(passOwnerIdFromDb, LocalDate.now())

        // THEN
        actual.collectList()
            .test()
            .expectNext(passesFromDb)
            .verifyComplete()

        verify { passRepository.findByOwnerAndPurchasedAfter(any(), any()) }
    }

    @Test
    fun `finding all passes by pass owner id should return corresponding passes`() {
        // GIVEN
        every { passRepository.findAllByPassOwnerId(any()) } returns passesFromDb.toFlux()

        // WHEN
        val actual = passService.findAllByPassOwnerId(passOwnerIdFromDb)

        // THEN
        actual.collectList()
            .test()
            .expectNext(passesFromDb)
            .verifyComplete()

        verify { passRepository.findAllByPassOwnerId(any()) }
    }
}
