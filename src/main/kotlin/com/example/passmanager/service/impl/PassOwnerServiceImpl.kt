package com.example.passmanager.service.impl

import com.example.passmanager.domain.MongoPassOwner
import com.example.passmanager.exception.PassOwnerNotFoundException
import com.example.passmanager.repositories.PassOwnerRepository
import com.example.passmanager.repositories.PassRepository
import com.example.passmanager.service.PassOwnerService
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
internal class PassOwnerServiceImpl(
    private val passOwnerRepository: PassOwnerRepository,
    private val passRepository: PassRepository,
) : PassOwnerService {

    override fun findById(passOwnerId: String): Mono<MongoPassOwner> {
        return passOwnerRepository.findById(passOwnerId)
    }

    override fun getById(passOwnerId: String): Mono<MongoPassOwner> {
        return findById(passOwnerId).switchIfEmpty(Mono.error(PassOwnerNotFoundException(passOwnerId)))
    }

    override fun create(newMongoPassOwner: MongoPassOwner): Mono<MongoPassOwner> {
        return passOwnerRepository.insert(newMongoPassOwner)
    }

    override fun update(newPassOwner: MongoPassOwner): Mono<MongoPassOwner> {
        return passOwnerRepository.save(newPassOwner)
    }

    override fun deleteById(passOwnerId: String): Mono<Unit> {
        return passOwnerRepository.deleteById(passOwnerId)
            .then(passRepository.deleteAllByOwnerId(passOwnerId))
    }
}
