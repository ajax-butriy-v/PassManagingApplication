package com.example.passmanagersvc.service.impl

import com.example.passmanagersvc.repositories.PassRepository
import com.example.passmanagersvc.service.PassManagementService
import com.example.passmanagersvc.service.PassOwnerService
import com.example.passmanagersvc.service.PassService
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2

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
            .flatMap { (pass, passOwner) -> passService.update(pass.copy(passOwnerId = passOwner.id)) }
            .thenReturn(Unit)
    }
}
