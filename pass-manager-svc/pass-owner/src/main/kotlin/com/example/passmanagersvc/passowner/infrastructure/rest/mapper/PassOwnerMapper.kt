package com.example.passmanagersvc.passowner.infrastructure.rest.mapper

import com.example.passmanagersvc.passowner.domain.PassOwner
import com.example.passmanagersvc.passowner.infrastructure.rest.dto.PassOwnerDto
import com.example.passmanagersvc.passowner.infrastructure.rest.dto.PassOwnerUpdateDto

object PassOwnerMapper {
    fun PassOwnerDto.toDomain(): PassOwner {
        return PassOwner(
            id = null,
            firstName = firstName.orEmpty(),
            lastName = lastName.orEmpty(),
            phoneNumber = phoneNumber.orEmpty(),
            email = email.orEmpty(),
            version = null
        )
    }

    fun PassOwner.toDto(): PassOwnerDto {
        return PassOwnerDto(
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber,
            email = email
        )
    }

    fun PassOwner.partialUpdate(passOwnerUpdateDto: PassOwnerUpdateDto): PassOwner {
        return copy(
            id = id,
            firstName = passOwnerUpdateDto.firstName ?: firstName,
            lastName = passOwnerUpdateDto.lastName ?: lastName,
            email = passOwnerUpdateDto.email ?: email,
            phoneNumber = passOwnerUpdateDto.phoneNumber ?: phoneNumber
        )
    }
}
