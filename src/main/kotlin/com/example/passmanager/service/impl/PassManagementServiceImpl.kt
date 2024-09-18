package com.example.passmanager.service.impl

import com.example.passmanager.repositories.PassRepository
import com.example.passmanager.service.PassManagementService
import com.example.passmanager.service.PassOwnerService
import com.example.passmanager.service.PassService
import org.springframework.stereotype.Service

@Service
internal class PassManagementServiceImpl(
    private val passOwnerService: PassOwnerService,
    private val passService: PassService,
    private val passRepository: PassRepository,
) : PassManagementService {
    override fun cancelPass(passOwnerId: String, passId: String): Boolean {
        return passRepository.deleteByIdAndOwnerId(passId, passOwnerId)
    }

    override fun transferPass(passId: String, targetPassOwnerId: String) {
        val passInDb = passService.getById(passId)
        val targetPassOwner = passOwnerService.getById(targetPassOwnerId)
        passService.update(passInDb.copy(passOwner = targetPassOwner))
    }
}
