package com.example.passmanager.service.impl

import com.example.passmanager.repositories.PassTypeRepository
import com.example.passmanager.util.PassFixture.singlePassType
import com.example.passmanager.util.PassFixture.singlePassTypeId
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
class PassTypeServiceImplTest {
    @MockK
    private lateinit var passTypeRepository: PassTypeRepository

    @InjectMockKs
    private lateinit var passTypeService: PassTypeServiceImpl

    @Test
    fun `creation should return new object with id`() {
        every { passTypeRepository.insert(any()) } returns singlePassType

        assertThat(passTypeService.create(singlePassType)).isEqualTo(singlePassType)

        verify { passTypeRepository.insert(any()) }
    }

    @Test
    fun `partial update with unique values should update object`() {
        // GIVEN
        val passTypeWithChangedName = singlePassType.copy(name = "Changed")
        every { passTypeRepository.save(any()) } returns passTypeWithChangedName

        // WHEN
        val updated = passTypeService.update(passTypeWithChangedName)
        assertThat(updated.name).isEqualTo("Changed")

        // THEN
        verify { passTypeRepository.save(any()) }
    }

    @Test
    fun `find by id should return object with specified id`() {
        every { passTypeRepository.findById(any()) } returns singlePassType

        assertThat(passTypeService.findById(singlePassTypeId)).isEqualTo(singlePassType)

        verify { passTypeRepository.findById(any()) }
    }

    @Test
    fun `delete by id should delete object`() {
        justRun { passTypeRepository.deleteById(any()) }

        passTypeService.deleteById(singlePassTypeId)

        verify { passTypeRepository.deleteById(any()) }
    }
}
