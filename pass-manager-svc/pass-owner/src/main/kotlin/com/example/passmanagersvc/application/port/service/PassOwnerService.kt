package com.example.passmanagersvc.application.port.service

import com.example.passmanagersvc.application.port.input.PassOwnerServiceInputPort
import com.example.passmanagersvc.domain.PassOwner
import reactor.core.publisher.Mono

class PassOwnerService : PassOwnerServiceInputPort {
    override fun findById(passOwnerId: String): Mono<PassOwner> {
        TODO("Not yet implemented")
    }

    override fun getById(passOwnerId: String): Mono<PassOwner> {
        TODO("Not yet implemented")
    }

    override fun create(newPassOwner: PassOwner): Mono<PassOwner> {
        TODO("Not yet implemented")
    }

    override fun update(passOwnerId: String, updatedPassOwner: PassOwner): Mono<PassOwner> {
        TODO("Not yet implemented")
    }

    override fun deleteById(passOwnerId: String): Mono<Unit> {
        TODO("Not yet implemented")
    }
}