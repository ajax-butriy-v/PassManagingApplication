package com.example.passmanager.web.mapper

import com.example.passmanager.util.PassFixture.dto
import com.example.passmanager.util.PassFixture.dtoWithValidIdFormats
import com.example.passmanager.util.PassFixture.passFromDb
import com.example.passmanager.util.PassFixture.passToCreate
import com.example.passmanager.web.mapper.PassMapper.toDto
import com.example.passmanager.web.mapper.PassMapper.toEntity
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class PassMapperTest {

    @Test
    fun `toDto() should return correctly mapped dto from entity object`() {
        // GIVEN WHEN
        val dtoFromEntity = passFromDb.toDto()

        // THEN
        assertThat(dtoFromEntity).usingRecursiveComparison().isEqualTo(dto)
    }

    @Test
    fun `toDto should set purchased for as zero if value is not specified`() {
        // GIVEN
        val dtoWithNullablePurchasedFor = passFromDb.copy(purchasedFor = null)

        // WHEN
        val dtoFromEntity = dtoWithNullablePurchasedFor.toDto()

        // THEN
        assertEquals(BigDecimal.ZERO, dtoFromEntity.purchasedFor)
    }

    @Test
    fun `toEntity should return correctly mapped entity from object`() {
        // GIVEN
        val expectedEntity = passToCreate.copy(
            passTypeId = ObjectId(dtoWithValidIdFormats.passTypeId),
            passOwnerId = ObjectId(dtoWithValidIdFormats.passOwnerId)
        )

        // WHEN
        val entityFromDto = dtoWithValidIdFormats.toEntity()

        // THEN
        assertEquals(expectedEntity.passOwnerId, entityFromDto.passOwnerId)
        assertEquals(expectedEntity.passTypeId, entityFromDto.passTypeId)
        assertEquals(expectedEntity.purchasedFor, entityFromDto.purchasedFor)
        assertNull(entityFromDto.id)
    }
}
