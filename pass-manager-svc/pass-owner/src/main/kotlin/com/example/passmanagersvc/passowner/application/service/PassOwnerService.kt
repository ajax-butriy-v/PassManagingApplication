package com.example.passmanagersvc.passowner.application.service

import com.example.core.exception.PassOwnerNotFoundException
import com.example.passmanagersvc.passowner.application.port.input.PassOwnerServiceInPort
import com.example.passmanagersvc.passowner.application.port.output.PassOwnerRepositoryOutPort
import com.example.passmanagersvc.passowner.domain.PassOwner
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class PassOwnerService(
    private val passOwnerRepositoryOutPort: PassOwnerRepositoryOutPort,
) : PassOwnerServiceInPort {
    override fun getById(passOwnerId: String): Mono<PassOwner> {
        return passOwnerRepositoryOutPort.findById(passOwnerId).switchIfEmpty {
            Mono.error(PassOwnerNotFoundException("Could not find pass owner by id $passOwnerId"))
        }
    }

    override fun create(newPassOwner: PassOwner): Mono<PassOwner> {
        return passOwnerRepositoryOutPort.insert(newPassOwner)
    }

    override fun update(passOwnerId: String, updatedPassOwner: PassOwner): Mono<PassOwner> {
        return passOwnerRepositoryOutPort.save(updatedPassOwner.copy(id = passOwnerId))
    }

    override fun deleteById(passOwnerId: String): Mono<Unit> {
        return passOwnerRepositoryOutPort.deleteById(passOwnerId)
    }
}
