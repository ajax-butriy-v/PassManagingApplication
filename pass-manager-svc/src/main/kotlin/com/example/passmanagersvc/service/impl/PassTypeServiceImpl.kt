package com.example.passmanagersvc.service.impl

import com.example.core.exception.PassTypeNotFoundException
import com.example.passmanagersvc.domain.MongoPassType
import com.example.passmanagersvc.repositories.PassTypeRepository
import com.example.passmanagersvc.service.PassTypeService
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
internal class PassTypeServiceImpl(private val passTypeRepository: PassTypeRepository) :
    PassTypeService {
    override fun findById(id: String): Mono<MongoPassType> {
        return passTypeRepository.findById(id)
    }

    override fun getById(id: String): Mono<MongoPassType> {
        return findById(id).switchIfEmpty {
            Mono.error(PassTypeNotFoundException("Could not find pass type by id $id"))
        }
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
