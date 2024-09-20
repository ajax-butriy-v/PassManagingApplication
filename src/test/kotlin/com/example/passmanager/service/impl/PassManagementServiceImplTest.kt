package com.example.passmanager.service.impl

import com.example.passmanager.repositories.PassRepository
import com.example.passmanager.service.PassOwnerService
import com.example.passmanager.service.PassService
import com.example.passmanager.util.PassFixture.passFromDb
import com.example.passmanager.util.PassFixture.singlePassId
import com.example.passmanager.util.PassOwnerFixture.passOwnerFromDb
import com.example.passmanager.util.PassOwnerFixture.passOwnerIdFromDb
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
internal class PassManagementServiceImplTest {
    @MockK
    private lateinit var passOwnerService: PassOwnerService

    @MockK
    private lateinit var passService: PassService

    @MockK
    private lateinit var passRepository: PassRepository

    @InjectMockKs
    private lateinit var passManagementService: PassManagementServiceImpl

    @Test
    fun `cancel pass should delete pass from client list if client id and pass is are valid`() {
        // GIVEN
        every { passRepository.deleteByIdAndOwnerId(any(), any()) } returns true
        // WHEN
        assertTrue("Pass, which belongs to owner, should be deleted") {
            passManagementService.cancelPass(passOwnerIdFromDb, singlePassId)
        }

        // THEN
        verify { passRepository.deleteByIdAndOwnerId(any(), any()) }
    }

    @Test
    fun `transferring pass to another client should complete if all ids are valid`() {
        // GIVEN
        every { passService.getById(any()) } returns passFromDb
        every { passOwnerService.getById(any()) } returns passOwnerFromDb
        every { passService.update(any()) } returns passFromDb

        // WHEN
        passManagementService.transferPass(singlePassId, passOwnerIdFromDb)

        // THEN
        verifyOrder {
            passService.getById(any())
            passOwnerService.getById(any())
            passService.update(any())
        }
    }
}
