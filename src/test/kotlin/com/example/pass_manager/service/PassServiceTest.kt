package com.example.pass_manager.service

import com.example.pass_manager.domain.MongoPass
import com.example.pass_manager.repositories.PassRepository
import com.example.pass_manager.service.impl.PassServiceImpl
import com.example.pass_manager.util.PassFixture.passFromDb
import com.example.pass_manager.util.PassFixture.passToCreate
import com.example.pass_manager.util.PassFixture.passes
import com.example.pass_manager.util.PassFixture.updatedPass
import com.example.pass_manager.util.PassOwnerFixture.passOwnerFromDb
import com.example.pass_manager.util.PassOwnerFixture.passOwnerId
import com.example.pass_manager.util.PassOwnerFixture.updatedOwner
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
import java.time.Instant

@ExtendWith(MockKExtension::class)
internal class PassServiceTest {
    @MockK
    private lateinit var passRepository: PassRepository

    @InjectMockKs
    private lateinit var passService: PassServiceImpl

    @Test
    fun `creation should return new object with id`() {
        every { passRepository.insert(any<MongoPass>()) } returns passFromDb

        assertThat(passService.create(passToCreate)).isEqualTo(passFromDb)

        verify { passRepository.insert(any<MongoPass>()) }
    }

    @Test
    fun `find by id should return object with specified id`() {
        every { passRepository.findByIdOrNull(any()) } returns passFromDb

        assertThat(passService.findById(passOwnerId)).isEqualTo(passFromDb)

        verify { passRepository.findByIdOrNull(any()) }
    }

    @Test
    fun `delete by id should delete object`() {
        justRun { passRepository.deleteById(any()) }

        passService.deleteById(passOwnerId)

        verify { passService.deleteById(any()) }
    }

    @Test
    fun `should return all passes for owner after particular date`() {
        every { passRepository.findAllByPassOwnerAndPurchasedAtAfter(any(), any()) } returns passes

        val afterDateList = passService.findAllByPassOwnerAndPurchasedAtAfter(passOwnerFromDb, Instant.MIN)
        assertThat(afterDateList).size().isEqualTo(3)

        verify { passRepository.findAllByPassOwnerAndPurchasedAtAfter(any(), any()) }
    }

    @Test
    fun findAllByPassOwnerId() {
        every { passRepository.findAllByPassOwnerId(any()) } returns passes

        val passesByOwner = passService.findAllByPassOwnerId(passOwnerId)
        assertThat(passesByOwner).size().isEqualTo(3)

        verify { passRepository.findAllByPassOwnerId(any()) }
    }

    @Test
    fun updateByPassOwner() {
        // GIVEN
        every {
            passRepository.updateMongoPassByPassOwner(
                any(), any()
            )
        } returns updatedPass

        // WHEN
        assertThat(passService.updateByPassOwner(passFromDb, updatedOwner)).isEqualTo(updatedPass)

        // THEN
        verify { passRepository.updateMongoPassByPassOwner(any(), any()) }
    }
}

