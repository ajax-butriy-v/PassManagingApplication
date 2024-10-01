package com.example.passmanager.service.impl

import com.example.passmanager.repositories.PassTypeRepository
import com.example.passmanager.util.PassFixture.singlePassType
import com.example.passmanager.util.PassFixture.singlePassTypeId
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test

@ExtendWith(MockKExtension::class)
class PassTypeServiceImplTest {
    @MockK
    private lateinit var passTypeRepository: PassTypeRepository

    @InjectMockKs
    private lateinit var passTypeService: PassTypeServiceImpl

    @Test
    fun `creation should return new object with id`() {
        // GIVEN
        every { passTypeRepository.insert(any()) } returns singlePassType.toMono()

        // WHEN
        val createdPassType = passTypeService.create(singlePassType)

        // THEN
        createdPassType.test()
            .expectNext(singlePassType)
            .verifyComplete()

        verify { passTypeRepository.insert(any()) }
    }

    @Test
    fun `partial update with unique values should update object`() {
        // GIVEN
        val passTypeWithChangedName = singlePassType.copy(name = "Changed")
        every { passTypeRepository.save(any()) } returns passTypeWithChangedName.toMono()

        // WHEN
        val updated = passTypeService.update(passTypeWithChangedName)

        // THEN
        updated.test()
            .assertNext { assertThat(it.name).isEqualTo("Changed") }
            .verifyComplete()
        verify { passTypeRepository.save(any()) }
    }

    @Test
    fun `find by id should return object with specified id`() {
        // GIVEN
        every { passTypeRepository.findById(any()) } returns singlePassType.toMono()

        // WHEN
        val passTypeById = passTypeService.findById(singlePassTypeId)

        // THEN
        passTypeById.test()
            .expectNext(singlePassType)
            .verifyComplete()
        verify { passTypeRepository.findById(any()) }
    }

    @Test
    fun `delete by id should delete object`() {
        // GIVEN
        every { passTypeRepository.deleteById(any()) } returns Unit.toMono()

        // WHEN
        val delete = passTypeService.deleteById(singlePassTypeId)

        // THEN
        delete.test()
            .expectNext(Unit)
            .verifyComplete()

        verify { passTypeRepository.deleteById(any()) }
    }
}
