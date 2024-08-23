package com.example.pass_manager.service.impl

import com.example.pass_manager.domain.MongoPassOwner
import com.example.pass_manager.repositories.PassOwnerRepository
import com.example.pass_manager.service.PassOwnerService
import org.bson.types.ObjectId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class PassOwnerServiceImpl(private val passOwnerRepository: PassOwnerRepository) : PassOwnerService {
    override fun findById(passOwnerId: ObjectId): MongoPassOwner? {
        return passOwnerRepository.findByIdOrNull(passOwnerId)
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

