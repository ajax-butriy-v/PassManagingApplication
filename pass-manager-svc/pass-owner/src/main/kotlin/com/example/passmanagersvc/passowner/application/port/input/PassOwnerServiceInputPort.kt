package com.example.passmanagersvc.passowner.application.port.input

import com.example.passmanagersvc.passowner.domain.PassOwner
import reactor.core.publisher.Mono

interface PassOwnerServiceInputPort {
    fun findById(passOwnerId: String): Mono<PassOwner>
    fun getById(passOwnerId: String): Mono<PassOwner>
    fun create(newPassOwner: PassOwner): Mono<PassOwner>
    fun update(passOwnerId: String, updatedPassOwner: PassOwner): Mono<PassOwner>
    fun deleteById(passOwnerId: String): Mono<Unit>
}
