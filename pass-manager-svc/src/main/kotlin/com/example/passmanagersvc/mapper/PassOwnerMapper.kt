package com.example.passmanagersvc.mapper

import com.example.passmanagersvc.domain.MongoPassOwner
import com.example.passmanagersvc.dto.PassOwnerDto
import com.example.passmanagersvc.dto.PassOwnerUpdateDto

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
