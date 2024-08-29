package com.example.passmanager.service.impl

import com.example.passmanager.exception.PassNotFoundException
import com.example.passmanager.service.PassManagementService
import com.example.passmanager.service.PassOwnerService
import com.example.passmanager.service.PassService
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
internal class PassManagementServiceImpl(
    private val passOwnerService: PassOwnerService,
    private val passService: PassService,
) : PassManagementService {
    override fun cancelPass(passOwnerId: ObjectId, passId: ObjectId): Boolean {
        val ownedPassesByOwner = passService.findAllByPassOwnerId(passOwnerId)
        val listContainsPass = ownedPassesByOwner.any { it.id == passId }
        if (listContainsPass) {
            passService.deleteById(passId)
        }
        return listContainsPass
    }

    override fun transferPass(passId: ObjectId, targetPassOwnerId: ObjectId) {
        val passInDb = passService.findById(passId) ?: throw PassNotFoundException(passId)
        val targetPassOwner = passOwnerService.getById(passId)
        passService.updateByPassOwner(passInDb, targetPassOwner)
    }
}
