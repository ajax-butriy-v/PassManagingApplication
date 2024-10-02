package com.example.passmanager.service.impl

import com.example.passmanager.repositories.PassRepository
import com.example.passmanager.service.PassManagementService
import com.example.passmanager.service.PassOwnerService
import com.example.passmanager.service.PassService
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
internal class PassManagementServiceImpl(
    private val passOwnerService: PassOwnerService,
    private val passService: PassService,
    private val passRepository: PassRepository,
) : PassManagementService {
    override fun cancelPass(passOwnerId: String, passId: String): Mono<Unit> {
        return passOwnerService.getById(passOwnerId).then(passRepository.deleteById(passId))
    }

    override fun transferPass(passId: String, targetPassOwnerId: String): Mono<Unit> {
        return Mono.zip(passService.getById(passId), passOwnerService.getById(targetPassOwnerId))
            .flatMap { passService.update(it.t1.copy(passOwnerId = it.t2.id)) }
            .thenReturn(Unit)
    }
}
