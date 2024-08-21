package com.example.pass_manager.service

import com.example.pass_manager.service.impl.PassManagementServiceImpl
import com.example.pass_manager.util.PassFixture.passFromDb
import com.example.pass_manager.util.PassFixture.passes
import com.example.pass_manager.util.PassFixture.singlePassId
import com.example.pass_manager.util.PassFixture.updatedPass
import com.example.pass_manager.util.PassOwnerFixture.passOwnerFromDb
import com.example.pass_manager.util.PassOwnerFixture.passOwnerId
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verifyOrder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
internal class PassManagementServiceTest {
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
        every { passOwnerService.findById(any()) } returns passOwnerFromDb
        every { passService.updateByPassOwner(passFromDb, passOwnerFromDb) } returns updatedPass

        // WHEN
        assertThat(passManagementService.transferPassToAnotherClient(singlePassId, passOwnerId)).isEqualTo(updatedPass)

        // THEN
        verifyOrder {
            passService.findById(any())
            passOwnerService.findById(any())
            passService.updateByPassOwner(any(), any())
        }
    }
}

