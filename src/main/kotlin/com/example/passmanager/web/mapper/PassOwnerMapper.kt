package com.example.passmanager.web.mapper

import com.example.passmanager.domain.MongoPassOwner
import com.example.passmanager.web.dto.PassOwnerDto

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
            email = email,
            ownedPasses = null
        )
    }

    fun MongoPassOwner.partialUpdate(dto: PassOwnerDto): MongoPassOwner {
        return copy(
            firstName = dto.firstName ?: firstName,
            lastName = dto.lastName ?: lastName,
            email = dto.email ?: email,
            phoneNumber = dto.phoneNumber ?: phoneNumber
        )
    }
}
