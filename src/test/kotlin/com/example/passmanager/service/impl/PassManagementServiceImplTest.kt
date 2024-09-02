package com.example.passmanager.service.impl

import com.example.passmanager.service.PassOwnerService
import com.example.passmanager.service.PassService
import com.example.passmanager.util.PassFixture.passFromDb
import com.example.passmanager.util.PassFixture.passes
import com.example.passmanager.util.PassFixture.singlePassId
import com.example.passmanager.util.PassOwnerFixture.passOwnerFromDb
import com.example.passmanager.util.PassOwnerFixture.passOwnerId
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
internal class PassManagementServiceImplTest {
    @MockK
    private lateinit var passOwnerService: PassOwnerService

    @MockK
    private lateinit var passService: PassService

    @InjectMockKs
    private lateinit var passManagementService: PassManagementServiceImpl

    @Test
    fun `cancel pass should delete pass from client list if client id and pass is are valid`() {
        // GIVEN
        every { passService.findAllByPassOwnerId(any()) } returns passes
        justRun { passService.deleteById(any()) }

        // WHEN
        assertTrue { passManagementService.cancelPass(passOwnerId, singlePassId) }

        // THEN
        verifyOrder {
            passService.findAllByPassOwnerId(any())
            passService.deleteById(any())
        }
    }

    @Test
    fun `transfering pass to another client should complete if all ids are valid`() {
        // GIVEN
        every { passService.findById(any()) } returns passFromDb
        every { passOwnerService.getById(any()) } returns passOwnerFromDb
        every { passService.update(any()) } returns passFromDb

        // WHEN
        passManagementService.transferPass(singlePassId, passOwnerId)

        // THEN
        verifyOrder {
            passService.findById(any())
            passOwnerService.getById(any())
            passService.update(any())
        }
    }
}
