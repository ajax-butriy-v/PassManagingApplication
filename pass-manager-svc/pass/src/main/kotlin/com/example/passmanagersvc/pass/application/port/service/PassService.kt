package com.example.passmanagersvc.pass.application.port.service

import com.example.core.exception.PassNotFoundException
import com.example.passmanagersvc.pass.application.port.input.PassServiceInputPort
import com.example.passmanagersvc.pass.application.port.output.PassRepositoryOutPort
import com.example.passmanagersvc.pass.domain.Pass
import com.example.passmanagersvc.passowner.application.port.input.PassOwnerServiceInputPort
import com.example.passmanagersvc.passtype.application.port.input.PassTypeServiceInPort
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.LocalDate

@Service
class PassService(
    private val passOwnerServiceInputPort: PassOwnerServiceInputPort,
    private val passTypeServiceInPort: PassTypeServiceInPort,
    private val passRepositoryOutPort: PassRepositoryOutPort,
) : PassServiceInputPort {
    override fun findById(passId: String): Mono<Pass> {
        return passRepositoryOutPort.findById(passId)
    }

    override fun getById(passId: String): Mono<Pass> {
        return findById(passId).switchIfEmpty {
            Mono.error(PassNotFoundException("Could not find pass by id $passId"))
        }
    }

    override fun create(newPass: Pass, ownerId: String, passTypeId: String): Mono<Pass> {
        return Mono.zip(passOwnerServiceInputPort.getById(ownerId), passTypeServiceInPort.getById(passTypeId)).flatMap {
            passRepositoryOutPort.insert(
                newPass.copy(
                    passOwnerId = ownerId,
                    passTypeId = passTypeId
                )
            )
        }
    }

    override fun update(pass: Pass): Mono<Pass> {
        return passRepositoryOutPort.save(pass)
    }

    override fun deleteById(passId: String): Mono<Unit> {
        return passRepositoryOutPort.deleteById(passId)
    }

    override fun deleteAllByOwnerId(passOwnerId: String): Mono<Unit> {
        return passRepositoryOutPort.deleteAllByOwnerId(passOwnerId)
    }

    override fun findAllByPassOwnerAndPurchasedAtGreaterThan(
        passOwnerId: String,
        afterDate: LocalDate,
    ): Flux<Pass> {
        return passRepositoryOutPort.findByOwnerAndPurchasedAfter(passOwnerId, afterDate)
    }

    override fun findAllByPassOwnerId(passOwnerId: String): Flux<Pass> {
        return passRepositoryOutPort.findAllByPassOwnerId(passOwnerId)
    }
}
