package com.example.passmanagersvc.application.service

import com.example.core.exception.PassTypeNotFoundException
import com.example.passmanagersvc.application.port.input.PassTypeServiceInPort
import com.example.passmanagersvc.application.port.out.PassTypeRepositoryOutPort
import com.example.passmanagersvc.domain.PassType
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class PassTypeService(private val passTypeRepositoryOutPort: PassTypeRepositoryOutPort) : PassTypeServiceInPort {
    override fun findById(id: String): Mono<PassType> {
        return passTypeRepositoryOutPort.findById(id)
    }

    override fun getById(id: String): Mono<PassType> {
        return findById(id).switchIfEmpty {
            Mono.error(PassTypeNotFoundException("Could not find pass type by id $id"))
        }
    }

    override fun create(passType: PassType): Mono<PassType> {
        return passTypeRepositoryOutPort.insert(passType)
    }

    override fun update(modifiedPassType: PassType): Mono<PassType> {
        return passTypeRepositoryOutPort.save(modifiedPassType)

    }

    override fun deleteById(id: String): Mono<Unit> {
        return passTypeRepositoryOutPort.deleteById(id)
    }
}