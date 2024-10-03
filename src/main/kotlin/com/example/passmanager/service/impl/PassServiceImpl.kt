package com.example.passmanager.service.impl

import com.example.passmanager.domain.MongoPass
import com.example.passmanager.exception.PassNotFoundException
import com.example.passmanager.repositories.PassRepository
import com.example.passmanager.service.PassOwnerService
import com.example.passmanager.service.PassService
import com.example.passmanager.service.PassTypeService
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import java.time.LocalDate

@Service
internal class PassServiceImpl(
    private val passOwnerService: PassOwnerService,
    private val passTypeService: PassTypeService,
    private val passRepository: PassRepository,
) : PassService {
    override fun findById(passId: String): Mono<MongoPass> {
        return passRepository.findById(passId)
    }

    override fun getById(passId: String): Mono<MongoPass> {
        return findById(passId).switchIfEmpty { Mono.error(PassNotFoundException(passId)) }
    }

    override fun create(newPass: MongoPass, ownerId: String, passTypeId: String): Mono<MongoPass> {
        return Mono.zip(passOwnerService.getById(ownerId), passTypeService.getById(passTypeId))
            .flatMap { (passOwner, passType) ->
                passRepository.insert(newPass.copy(passOwnerId = passOwner.id, passTypeId = passType.id))
            }
    }

    override fun update(pass: MongoPass): Mono<MongoPass> {
        return passRepository.save(pass)
    }

    override fun deleteById(passId: String): Mono<Unit> {
        return passRepository.deleteById(passId)
    }

    override fun deleteAllByOwnerId(passOwnerId: String): Mono<Unit> {
        return passRepository.deleteAllByOwnerId(passOwnerId)
    }

    override fun findAllByPassOwnerAndPurchasedAtGreaterThan(
        passOwnerId: String,
        afterDate: LocalDate,
    ): Flux<MongoPass> {
        return passRepository.findByOwnerAndPurchasedAfter(passOwnerId, afterDate)
    }

    override fun findAllByPassOwnerId(passOwnerId: String): Flux<MongoPass> {
        return passRepository.findAllByPassOwnerId(passOwnerId)
    }
}
