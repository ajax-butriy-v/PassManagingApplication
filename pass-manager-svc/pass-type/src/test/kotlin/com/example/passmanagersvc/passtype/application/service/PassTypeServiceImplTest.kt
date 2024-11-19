package com.example.passmanagersvc.passtype.application.service

import com.example.core.exception.PassTypeNotFoundException
import com.example.passmanagersvc.passtype.application.port.out.PassTypeRepositoryOutPort
import com.example.passmanagersvc.util.PassFixture.singlePassType
import com.example.passmanagersvc.util.PassFixture.singlePassTypeId
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
internal class PassTypeServiceImplTest {
    @MockK
    private lateinit var passTypeRepositoryOutPort: PassTypeRepositoryOutPort

    @InjectMockKs
    private lateinit var passTypeService: PassTypeService

    @Test
    fun `creation should return new object with id`() {
        // GIVEN
        every { passTypeRepositoryOutPort.insert(any()) } returns singlePassType.toMono()

        // WHEN
        val createdPassType = passTypeService.create(singlePassType)

        // THEN
        createdPassType.test()
            .expectNext(singlePassType)
            .verifyComplete()

        verify { passTypeRepositoryOutPort.insert(any()) }
    }

    @Test
    fun `partial update with unique values should update object`() {
        // GIVEN
        val passTypeWithChangedName = singlePassType.copy(name = "Changed")
        every { passTypeRepositoryOutPort.save(any()) } returns passTypeWithChangedName.toMono()

        // WHEN
        val updated = passTypeService.update(passTypeWithChangedName)

        // THEN
        updated.test()
            .assertNext { assertThat(it.name).isEqualTo("Changed") }
            .verifyComplete()
        verify { passTypeRepositoryOutPort.save(any()) }
    }

    @Test
    fun `find by id should return object with specified id`() {
        // GIVEN
        every { passTypeRepositoryOutPort.findById(any()) } returns singlePassType.toMono()

        // WHEN
        val passTypeById = passTypeService.findById(singlePassTypeId)

        // THEN
        passTypeById.test()
            .expectNext(singlePassType)
            .verifyComplete()
        verify { passTypeRepositoryOutPort.findById(any()) }
    }

    @Test
    fun `delete by id should delete object`() {
        // GIVEN
        every { passTypeRepositoryOutPort.deleteById(any()) } returns Unit.toMono()

        // WHEN
        val delete = passTypeService.deleteById(singlePassTypeId)

        // THEN
        delete.test()
            .expectNext(Unit)
            .verifyComplete()

        verify { passTypeRepositoryOutPort.deleteById(any()) }
    }

    @Test
    fun `get by id should return value if object is present in db`() {
        // GIVEN
        every { passTypeRepositoryOutPort.findById(any()) } returns singlePassType.toMono()

        // WHEN
        val passTypeById = passTypeService.getById(singlePassTypeId)

        // THEN
        passTypeById.test()
            .expectNext(singlePassType)
            .verifyComplete()

        verify { passTypeRepositoryOutPort.findById(any()) }
    }

    @Test
    fun `get by id should return throw error if object is not present in db`() {
        // GIVEN
        every { passTypeRepositoryOutPort.findById(any()) } returns Mono.empty()

        // WHEN
        val passTypeById = passTypeService.getById(singlePassTypeId)

        // THEN
        passTypeById.test().verifyError<PassTypeNotFoundException>()

        verify { passTypeRepositoryOutPort.findById(any()) }
    }
}
