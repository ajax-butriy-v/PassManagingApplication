package com.example.passmanager.service.impl

import com.example.passmanager.domain.MongoPassType
import com.example.passmanager.exception.PassTypeNotFoundException
import com.example.passmanager.repositories.PassTypeRepository
import com.example.passmanager.service.PassTypeService
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
internal class PassTypeServiceImpl(private val passTypeRepository: PassTypeRepository) : PassTypeService {
    override fun findById(id: String): Mono<MongoPassType> {
        return passTypeRepository.findById(id)
    }

    override fun getById(id: String): Mono<MongoPassType> {
        return findById(id).switchIfEmpty { Mono.error(PassTypeNotFoundException(id)) }
    }

    override fun create(passType: MongoPassType): Mono<MongoPassType> {
        return passTypeRepository.insert(passType)
    }

    override fun update(modifiedPassType: MongoPassType): Mono<MongoPassType> {
        return passTypeRepository.save(modifiedPassType)
    }

    override fun deleteById(id: String): Mono<Unit> {
        return passTypeRepository.deleteById(id)
    }
}
