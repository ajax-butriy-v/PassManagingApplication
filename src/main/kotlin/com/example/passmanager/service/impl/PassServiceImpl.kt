package com.example.passmanager.service.impl

import com.example.passmanager.domain.MongoPass
import com.example.passmanager.domain.MongoPassOwner
import com.example.passmanager.repositories.PassRepository
import com.example.passmanager.service.PassOwnerService
import com.example.passmanager.service.PassService
import com.example.passmanager.service.PassTypeService
import org.bson.types.ObjectId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant

@Service
internal class PassServiceImpl(
    private val passRepository: PassRepository,
    private val passOwnerService: PassOwnerService,
    private val passTypeService: PassTypeService,
) :
    PassService {
    override fun findById(passId: ObjectId): MongoPass? {
        return passRepository.findByIdOrNull(passId)
    }

    override fun create(newPass: MongoPass, ownerId: ObjectId, passTypeId: ObjectId): MongoPass {
        val passOwner = passOwnerService.getById(ownerId)
        val passType = passTypeService.getById(passTypeId)
        return passRepository.insert(newPass.copy(passOwner = passOwner, passType = passType))
    }

    override fun deleteById(passId: ObjectId) {
        passRepository.deleteById(passId)
    }

    override fun findAllByPassOwnerAndPurchasedAtGreaterThan(
        passOwner: MongoPassOwner,
        afterDate: Instant,
    ): List<MongoPass> {
        return passRepository.findAllByPassOwnerAndPurchasedAtGreaterThan(passOwner, afterDate)
    }

    override fun findAllByPassOwnerId(passOwnerId: ObjectId): List<MongoPass> {
        return passRepository.findAllByPassOwnerId(passOwnerId)
    }

    override fun updateByPassOwner(pass: MongoPass, passOwner: MongoPassOwner) {
        passRepository.updateMongoPassByPassOwner(pass, passOwner)
    }
}
