package com.example.pass_manager.web.mapper

import com.example.pass_manager.domain.MongoClient
import com.example.pass_manager.web.dto.MongoClientDto


object MongoClientMapper {
    fun MongoClient.toDto(): MongoClientDto {
        return MongoClientDto(firstName, lastName, phoneNumber, email)
    }

    fun MongoClientDto.toEntity(): MongoClient {
        return MongoClient(
            firstName = firstName,
            id = null,
            lastName = lastName,
            phoneNumber = phoneNumber,
            email = email,
            ownedPasses = null
        )
    }

    fun MongoClient.partialUpdate(dto: MongoClientDto): MongoClient {
        return copy(
            firstName = dto.firstName ?: firstName,
            lastName = dto.lastName ?: lastName,
            email = dto.email ?: email,
            phoneNumber = dto.phoneNumber ?: phoneNumber
        )
    }
}
