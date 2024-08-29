package com.example.passmanager.service.impl

import com.example.passmanager.domain.MongoPassOwner
import com.example.passmanager.exception.PassOwnerNotFoundException
import com.example.passmanager.repositories.PassOwnerRepository
import com.example.passmanager.service.PassOwnerService
import org.bson.types.ObjectId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
internal class PassOwnerServiceImpl(private val passOwnerRepository: PassOwnerRepository) : PassOwnerService {
    override fun findById(passOwnerId: ObjectId): MongoPassOwner? {
        return passOwnerRepository.findByIdOrNull(passOwnerId)
    }

    override fun getById(passOwnerId: ObjectId): MongoPassOwner {
        return findById(passOwnerId) ?: throw PassOwnerNotFoundException(passOwnerId)
    }

    override fun create(newMongoPassOwner: MongoPassOwner): MongoPassOwner {
        return passOwnerRepository.insert(newMongoPassOwner)
    }

    override fun update(passOwnerId: ObjectId, modifiedMongoPassOwner: MongoPassOwner): MongoPassOwner {
        return passOwnerRepository.save(modifiedMongoPassOwner)
    }

    override fun deleteById(passOwnerId: ObjectId) {
        passOwnerRepository.deleteById(passOwnerId)
    }
}
