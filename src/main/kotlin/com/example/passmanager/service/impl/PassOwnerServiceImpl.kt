package com.example.passmanager.service.impl

import com.example.passmanager.domain.MongoPassOwner
import com.example.passmanager.exception.PassOwnerNotFoundException
import com.example.passmanager.repositories.PassOwnerRepository
import com.example.passmanager.repositories.PassRepository
import com.example.passmanager.service.PassOwnerService
import org.springframework.stereotype.Service

@Service
internal class PassOwnerServiceImpl(
    private val passOwnerRepository: PassOwnerRepository,
    private val passRepository: PassRepository,
) : PassOwnerService {

    override fun findById(passOwnerId: String): MongoPassOwner? {
        return passOwnerRepository.findById(passOwnerId)
    }

    override fun getById(passOwnerId: String): MongoPassOwner {
        return findById(passOwnerId) ?: throw PassOwnerNotFoundException(passOwnerId)
    }

    override fun create(newMongoPassOwner: MongoPassOwner): MongoPassOwner {
        return passOwnerRepository.insert(newMongoPassOwner)
    }

    override fun update(passOwnerId: String, modifiedMongoPassOwner: MongoPassOwner): MongoPassOwner {
        return passOwnerRepository.save(modifiedMongoPassOwner)
    }

    override fun deleteById(passOwnerId: String) {
        passOwnerRepository.deleteById(passOwnerId)
        passRepository.deleteAllByOwnerId(passOwnerId)
    }
}
