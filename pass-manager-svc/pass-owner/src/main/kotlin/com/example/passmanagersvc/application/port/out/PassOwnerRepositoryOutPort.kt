package com.example.passmanagersvc.application.port.out

import com.example.passmanagersvc.domain.PassOwner
import reactor.core.publisher.Mono

interface PassOwnerRepositoryOutPort {
    fun findById(passOwnerId: String): Mono<PassOwner>
    fun insert(newPassOwner: PassOwner): Mono<PassOwner>
    fun deleteById(passOwnerId: String): Mono<Unit>
    fun save(newPassOwner: PassOwner): Mono<PassOwner>
}