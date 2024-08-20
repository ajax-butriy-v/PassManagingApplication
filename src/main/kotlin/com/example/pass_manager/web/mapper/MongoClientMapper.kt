package com.example.pass_manager.web.mapper

import com.example.pass_manager.domain.Client
import com.example.pass_manager.web.dto.ClientDto


object MongoClientMapper {
    fun Client.toDto(): ClientDto {
        return ClientDto(firstName, lastName, phoneNumber, email)
    }

    fun ClientDto.toEntity(): Client {
        return Client(
            firstName = firstName,
            id = null,
            lastName = lastName,
            phoneNumber = phoneNumber,
            email = email,
            ownedPasses = null
        )
    }

    fun Client.partialUpdate(dto: ClientDto): Client {
        return copy(
            firstName = dto.firstName ?: firstName,
            lastName = dto.lastName ?: lastName,
            email = dto.email ?: email,
            phoneNumber = dto.phoneNumber ?: phoneNumber
        )
    }
}
