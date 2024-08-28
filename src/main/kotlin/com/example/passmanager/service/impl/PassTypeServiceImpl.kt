package com.example.passmanager.service.impl

import com.example.passmanager.domain.MongoPassType
import com.example.passmanager.exception.PassTypeNotFoundException
import com.example.passmanager.repositories.PassTypeRepository
import com.example.passmanager.service.PassTypeService
import org.bson.types.ObjectId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class PassTypeServiceImpl(private val passTypeRepository: PassTypeRepository) : PassTypeService {
    override fun findById(id: ObjectId): MongoPassType? {
        return passTypeRepository.findByIdOrNull(id)
    }

    override fun getById(id: ObjectId): MongoPassType {
        return findById(id) ?: throw PassTypeNotFoundException(id)
    }

    override fun create(passType: MongoPassType): MongoPassType {
        return passTypeRepository.insert(passType)
    }

    override fun deleteById(id: ObjectId) {
        passTypeRepository.deleteById(id)
    }

}

