package com.example.passmanager.service.impl

import com.example.passmanager.domain.MongoPass
import com.example.passmanager.exception.PassNotFoundException
import com.example.passmanager.repositories.PassRepository
import com.example.passmanager.service.PassOwnerService
import com.example.passmanager.service.PassService
import com.example.passmanager.service.PassTypeService
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
internal class PassServiceImpl(
    private val passOwnerService: PassOwnerService,
    private val passTypeService: PassTypeService,
    private val passRepository: PassRepository,
) : PassService {
    override fun findById(passId: String): MongoPass? {
        return passRepository.findById(passId)
    }

    override fun getById(passId: String): MongoPass {
        return findById(passId) ?: throw PassNotFoundException(passId)
    }

    override fun create(newPass: MongoPass, ownerId: String, passTypeId: String): MongoPass {
        val passOwner = passOwnerService.getById(ownerId)
        val passType = passTypeService.getById(passTypeId)
        return passRepository.insert(newPass.copy(passOwner = passOwner, passType = passType))
    }

    override fun update(pass: MongoPass): MongoPass {
        return passRepository.save(pass)
    }

    override fun deleteById(passId: String) {
        passRepository.deleteById(passId)
    }

    override fun deleteAllByOwnerId(passOwnerId: String) {
        passRepository.deleteAllByOwnerId(passOwnerId)
    }

    override fun findAllByPassOwnerAndPurchasedAtGreaterThan(
        passOwnerId: String,
        afterDate: LocalDate,
    ): List<MongoPass> {
        return passRepository.findByOwnerAndPurchasedAfter(passOwnerId, afterDate)
    }

    override fun findAllByPassOwnerId(passOwnerId: String): List<MongoPass> {
        return passRepository.findAllByPassOwnerId(passOwnerId)
    }
}
