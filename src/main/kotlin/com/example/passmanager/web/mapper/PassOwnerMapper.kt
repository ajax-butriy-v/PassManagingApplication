package com.example.passmanager.web.mapper

import com.example.passmanager.domain.MongoPassOwner
import com.example.passmanager.web.dto.PassOwnerDto
import com.example.passmanager.web.dto.PassOwnerUpdateDto

internal object PassOwnerMapper {
    fun MongoPassOwner.toDto(): PassOwnerDto {
        return PassOwnerDto(firstName, lastName, phoneNumber, email)
    }

    fun PassOwnerDto.toEntity(): MongoPassOwner {
        return MongoPassOwner(
            firstName = firstName,
            id = null,
            lastName = lastName,
            phoneNumber = phoneNumber,
            email = email
        )
    }

    fun MongoPassOwner.partialUpdate(passOwnerUpdateDto: PassOwnerUpdateDto): MongoPassOwner {
        return copy(
            firstName = passOwnerUpdateDto.firstName ?: firstName,
            lastName = passOwnerUpdateDto.lastName ?: lastName,
            email = passOwnerUpdateDto.email ?: email,
            phoneNumber = passOwnerUpdateDto.phoneNumber ?: phoneNumber
        )
    }
}
