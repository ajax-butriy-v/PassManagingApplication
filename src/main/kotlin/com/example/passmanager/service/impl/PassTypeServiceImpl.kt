package com.example.passmanager.service.impl

import com.example.passmanager.domain.MongoPassType
import com.example.passmanager.exception.PassTypeNotFoundException
import com.example.passmanager.repositories.PassTypeRepository
import com.example.passmanager.service.PassTypeService
import org.springframework.stereotype.Service

@Service
internal class PassTypeServiceImpl(private val passTypeRepository: PassTypeRepository) : PassTypeService {
    override fun findById(id: String): MongoPassType? {
        return passTypeRepository.findById(id)
    }

    override fun getById(id: String): MongoPassType {
        return findById(id) ?: throw PassTypeNotFoundException(id)
    }

    override fun create(passType: MongoPassType): MongoPassType {
        return passTypeRepository.insert(passType)
    }

    override fun deleteById(id: String) {
        passTypeRepository.deleteById(id)
    }
}
