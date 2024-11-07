package com.example.passmanagersvc.service.impl

import com.example.core.exception.PassOwnerNotFoundException
import com.example.passmanagersvc.domain.MongoPassOwner
import com.example.passmanagersvc.dto.PassOwnerUpdateDto
import com.example.passmanagersvc.mapper.PassOwnerMapper.partialUpdate
import com.example.passmanagersvc.repositories.PassOwnerRepository
import com.example.passmanagersvc.repositories.PassRepository
import com.example.passmanagersvc.service.PassOwnerService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
internal class PassOwnerServiceImpl(
    @Qualifier("redisPassOwnerRepository")
    private val passOwnerRepository: PassOwnerRepository,
    @Qualifier("redisPassRepository")
    private val passRepository: PassRepository,
) : PassOwnerService {

    override fun findById(passOwnerId: String): Mono<MongoPassOwner> {
        return passOwnerRepository.findById(passOwnerId)
    }

    override fun getById(passOwnerId: String): Mono<MongoPassOwner> {
        return findById(passOwnerId).switchIfEmpty {
            Mono.error(PassOwnerNotFoundException("Could not find pass owner by id $passOwnerId"))
        }
    }

    override fun create(newMongoPassOwner: MongoPassOwner): Mono<MongoPassOwner> {
        return passOwnerRepository.insert(newMongoPassOwner)
    }

    override fun update(passOwnerId: String, passOwnerUpdateDto: PassOwnerUpdateDto): Mono<MongoPassOwner> {
        return getById(passOwnerId)
            .map { passOwnerFromDb -> passOwnerFromDb.partialUpdate(passOwnerUpdateDto) }
            .flatMap { partiallyUpdated -> passOwnerRepository.save(partiallyUpdated) }
    }

    override fun deleteById(passOwnerId: String): Mono<Unit> {
        return passOwnerRepository.deleteById(passOwnerId)
            .then(passRepository.deleteAllByOwnerId(passOwnerId))
    }
}
