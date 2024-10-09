package com.example.passmanager.web.mapper

import com.example.passmanager.util.PassOwnerFixture.passOwnerDto
import com.example.passmanager.util.PassOwnerFixture.passOwnerFromDb
import com.example.passmanager.util.PassOwnerFixture.passOwnerUpdateDto
import com.example.passmanager.web.dto.PassOwnerUpdateDto
import com.example.passmanager.web.mapper.PassOwnerMapper.partialUpdate
import com.example.passmanager.web.mapper.PassOwnerMapper.toDto
import com.example.passmanager.web.mapper.PassOwnerMapper.toEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class PassOwnerMapperTest {
    @Test
    fun `testing dto converting from MongoPassOwner to PassOwnerDto`() {
        // GIVEN
        val entity = passOwnerFromDb

        // WHEN
        val dto = entity.toDto()
        assertThat(dto).usingRecursiveAssertion().isEqualTo(passOwnerDto)
    }

    @Test
    fun `testing entity converting from PassOwnerDto to MongoPassOwner`() {
        // GIVEN
        val dto = passOwnerDto

        // WHEN
        val entity = dto.toEntity()
        val expected = passOwnerFromDb.copy(id = null)
        assertThat(entity).usingRecursiveAssertion().isEqualTo(expected)
    }

    @Test
    fun `testing partial update updates only non-null fields`() {
        // GIVEN
        val partiallyUpdated = passOwnerUpdateDto.copy(firstName = "Updated")

        // WHEN
        val actual = passOwnerFromDb.partialUpdate(partiallyUpdated)
        assertThat(actual.firstName).isEqualTo("Updated")
        assertThat(actual).isNotEqualTo(passOwnerFromDb)
    }

    @Test
    fun `test partialUpdate when all fields are null does not update any field`() {
        // GIVEN
        val allFieldsNullableDto = PassOwnerUpdateDto(
            firstName = null,
            lastName = null,
            phoneNumber = null,
            email = null
        )

        // WHEN
        val actual = passOwnerFromDb.partialUpdate(allFieldsNullableDto)

        assertThat(actual).usingRecursiveAssertion().isEqualTo(passOwnerFromDb)
            .hasNoNullFields()
    }
}
