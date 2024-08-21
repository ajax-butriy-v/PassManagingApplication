package com.example.pass_manager.service.impl

import com.example.pass_manager.domain.MongoPass
import com.example.pass_manager.domain.MongoPassOwner
import com.example.pass_manager.repositories.PassRepository
import com.example.pass_manager.service.PassService
import org.bson.types.ObjectId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class PassServiceImpl(private val passRepository: PassRepository, ) : PassService {
    override fun findById(passId: ObjectId): MongoPass? = passRepository.findByIdOrNull(passId)

    override fun create(newPass: MongoPass): MongoPass = passRepository.insert(newPass)

    override fun deleteById(passId: ObjectId) {
        passRepository.deleteById(passId)
    }

    override fun findAllByPassOwnerAndPurchasedAtAfter(passOwner: MongoPassOwner, afterDate: Instant): List<MongoPass> {
        return passRepository.findAllByPassOwnerAndPurchasedAtAfter(passOwner, afterDate)
    }

    override fun findAllByPassOwnerId(passOwnerId: ObjectId): List<MongoPass> {
        return passRepository.findAllByPassOwnerId(passOwnerId)
    }

    override fun updateByPassOwner(pass: MongoPass, passOwner: MongoPassOwner): MongoPass {
        return passRepository.updateMongoPassByPassOwner(pass, passOwner)
    }

}

