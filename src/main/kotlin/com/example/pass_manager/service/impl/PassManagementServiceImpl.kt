package com.example.pass_manager.service.impl

import com.example.pass_manager.exception.PassNotFoundException
import com.example.pass_manager.exception.PassOwnerNotFoundException
import com.example.pass_manager.service.PassManagementService
import com.example.pass_manager.service.PassOwnerService
import com.example.pass_manager.service.PassService
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class PassManagementServiceImpl(
    private val passOwnerService: PassOwnerService,
    private val passService: PassService,
) : PassManagementService {
    override fun cancelPass(passOwnerId: ObjectId, passId: ObjectId): Boolean {
        val ownedPassesByOwner = passService.findAllByPassOwnerId(passOwnerId)
        val listContainsPass = ownedPassesByOwner.any { it.id == passId }
        if (listContainsPass)
            passService.deleteById(passId)
        return listContainsPass
    }


    override fun transferPass(passId: ObjectId, targetPassOwnerId: ObjectId) {
        val passInDb = passService.findById(passId) ?: throw PassNotFoundException(passId)
        val targetClient = passOwnerService.findById(targetPassOwnerId)
        targetClient?.also { passService.updateByPassOwner(passInDb, it) }
            ?: throw PassOwnerNotFoundException(targetPassOwnerId)
    }
}

