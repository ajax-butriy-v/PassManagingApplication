package com.example.pass_manager.web.mapper

import com.example.pass_manager.domain.MongoPassOwner
import com.example.pass_manager.web.dto.PassOwnerDto


object PassOwnerMapper {
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

