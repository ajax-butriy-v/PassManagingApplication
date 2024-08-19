package com.example.pass_manager.service

import com.example.pass_manager.domain.MongoPass
import com.example.pass_manager.exception.ClientNotFoundException
import com.example.pass_manager.exception.PassNotFoundException
import com.example.pass_manager.repositories.PassRepository
import com.example.pass_manager.web.dto.PriceDistribution
import org.bson.types.ObjectId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.math.BigDecimal


@Service
class PassServiceImpl(
    private val clientService: ClientService,
    private val passRepository: PassRepository,
) : PassService {
    override fun findById(passId: ObjectId): MongoPass? = passRepository.findByIdOrNull(passId)

    override fun create(newPass: MongoPass): MongoPass = passRepository.insert(newPass)

    override fun deleteById(passId: ObjectId) {
        passRepository.deleteById(passId)
    }

    override fun calculatePriceDistribution(clientId: ObjectId): List<PriceDistribution> {
        val passTypeWithTotalMap = passRepository.findAllByClientId(clientId)
            .map { pass -> pass.passType to pass.purchasedFor }
            .groupBy({ (passType, _) -> passType?.name }, { it.second })
            .mapValues { (_, prices) -> prices.sumOf { price -> price ?: BigDecimal.ZERO } }

        return passTypeWithTotalMap.map { PriceDistribution(it.key, it.value) }
    }

    override fun transferPassToAnotherClient(passId: ObjectId, targetClientId: ObjectId) {
        val passInDb = findById(passId) ?: throw PassNotFoundException(passId)
        val targetClient = clientService.findById(targetClientId)
        targetClient?.also { passRepository.updateMongoPassByClient(passInDb, targetClient) }
            ?: throw ClientNotFoundException(targetClientId)
    }
}
