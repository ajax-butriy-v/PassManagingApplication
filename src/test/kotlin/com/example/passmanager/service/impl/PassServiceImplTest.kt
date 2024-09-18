package com.example.passmanager.service.impl

import com.example.passmanager.exception.PassNotFoundException
import com.example.passmanager.repositories.PassRepository
import com.example.passmanager.service.PassOwnerService
import com.example.passmanager.service.PassTypeService
import com.example.passmanager.util.PassFixture.passFromDb
import com.example.passmanager.util.PassFixture.passToCreate
import com.example.passmanager.util.PassFixture.passes
import com.example.passmanager.util.PassFixture.singlePassId
import com.example.passmanager.util.PassFixture.singlePassType
import com.example.passmanager.util.PassFixture.singlePassTypeId
import com.example.passmanager.util.PassOwnerFixture.passOwnerFromDb
import com.example.passmanager.util.PassOwnerFixture.passOwnerIdFromDb
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
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
        every { passOwnerService.getById(passOwnerIdFromDb) } returns passOwnerFromDb
        every { passTypeService.getById(singlePassTypeId) } returns singlePassType
        every { passRepository.insert(any()) } returns passFromDb

        assertThat(passService.create(passToCreate, passOwnerIdFromDb, singlePassTypeId)).isEqualTo(passFromDb)

        verify { passRepository.insert(any()) }
    }

    @Test
    fun `find by id should return object with specified id`() {
        every { passRepository.findById(any()) } returns passFromDb

        assertThat(passService.findById(singlePassId)).isEqualTo(passFromDb)

        verify { passRepository.findById(any()) }
    }

    @Test
    fun `delete by id should delete object`() {
        justRun { passRepository.deleteById(any()) }

        passService.deleteById(singlePassId)

        verify { passService.deleteById(any()) }
    }

    @Test
    fun `should return all passes for owner after particular date`() {
        every { passRepository.findByOwnerAndPurchasedAfter(any(), any()) } returns passes

        val afterDateList = passService.findAllByPassOwnerAndPurchasedAtGreaterThan(passOwnerIdFromDb, LocalDate.MIN)
        assertThat(afterDateList).size().isEqualTo(3)

        verify { passRepository.findByOwnerAndPurchasedAfter(any(), any()) }
    }

    @Test
    fun `finding all passes by pass owner id should return all corresponding passes`() {
        every { passRepository.findAllByPassOwnerId(any()) } returns passes

        val passesByOwner = passService.findAllByPassOwnerId(passOwnerIdFromDb)
        assertThat(passesByOwner).size().isEqualTo(3)

        verify { passRepository.findAllByPassOwnerId(any()) }
    }

    @Test
    fun `updating pass should return updated object`() {
        val changedPass = passFromDb.copy(purchasedFor = BigDecimal.valueOf(200))

        // GIVEN
        every { passRepository.save(any()) } returns changedPass

        // WHEN
        val actual = passService.update(changedPass)
        assertThat(actual).isEqualTo(changedPass)

        // THEN
        verify { passRepository.save(any()) }
    }

    @Test
    fun `get by id should return pass if it exists`() {
        // GIVEN
        every { passRepository.findById(any()) } returns passFromDb

        // WHEN
        assertThat(passService.getById(singlePassId)).isEqualTo(passFromDb)

        // THEN
        verify { passRepository.findById(any()) }
    }

    @Test
    fun `get by id should throw exception if pass not exist`() {
        // GIVEN
        every { passRepository.findById(any()) } returns null

        // WHEN
        assertThrows<PassNotFoundException> { passService.getById(singlePassId) }
    }

    @Test
    fun `deleting all passes by pass owner id should delete all corresponding passes`() {
        // GIVEN
        justRun { passRepository.deleteAllByOwnerId(any()) }

        // WHEN
        passService.deleteAllByOwnerId(singlePassId)

        // THEN
        verify { passRepository.deleteAllByOwnerId(any()) }
    }

    @Test
    fun `finding all by pass owner and purchased at should return corresponding passes`() {
        // GIVEN
        every { passRepository.findByOwnerAndPurchasedAfter(any(), any()) } returns passes

        // WHEN
        val actual = passService.findAllByPassOwnerAndPurchasedAtGreaterThan(passOwnerIdFromDb, LocalDate.now())
        assertThat(actual).isEqualTo(passes)

        // THEN
        verify { passRepository.findByOwnerAndPurchasedAfter(any(), any()) }
    }

    @Test
    fun `finding all passes by pass owner id should return corresponding passes`() {
        // GIVEN
        every { passRepository.findAllByPassOwnerId(any()) } returns passes

        // WHEN
        val actual = passService.findAllByPassOwnerId(passOwnerIdFromDb)
        assertThat(actual).isEqualTo(passes)

        // THEN
        verify { passRepository.findAllByPassOwnerId(any()) }
    }
}
