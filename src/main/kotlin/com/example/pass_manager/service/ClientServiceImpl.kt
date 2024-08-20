package com.example.pass_manager.service

import com.example.pass_manager.domain.Client
import com.example.pass_manager.exception.ClientAlreadyExistsException
import com.example.pass_manager.exception.ClientNotFoundException
import com.example.pass_manager.repositories.ClientRepository
import com.example.pass_manager.repositories.PassRepository
import org.bson.types.ObjectId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant

@Service
class ClientServiceImpl(
    private val clientRepository: ClientRepository,
    private val passRepository: PassRepository,
) : ClientService {
    override fun findById(clientId: ObjectId): Client? = clientRepository.findByIdOrNull(clientId)

    override fun create(newClient: Client): Client {
        return newClient.ifUnique(
            { !clientRepository.existsByEmailOrPhoneNumber(email ?: "", phoneNumber ?: "") },
            { uniqueClient -> clientRepository.insert(uniqueClient) }
        )
    }

    override fun update(clientId: ObjectId, modifiedClient: Client): Client {
        return modifiedClient.ifUnique({
            val clientWithSameCredentialsInDb = clientRepository.findByEmailAndPhoneNumber(
                email ?: "",
                phoneNumber ?: ""
            )
            clientWithSameCredentialsInDb?.id != id
        }, { uniqueClient -> clientRepository.save(uniqueClient) })
    }

    override fun cancelPass(clientId: ObjectId, passId: ObjectId): Boolean {
        val ownedPassesByClient = findById(clientId)?.ownedPasses
        return ownedPassesByClient?.let { passes ->
            val isInList = passes.map { it.id }.contains(passId)
            passRepository.deleteById(passId)
            isInList
        } ?: throw ClientNotFoundException(clientId)
    }

    override fun calculateSpentAfterDate(afterDate: Instant, clientId: ObjectId): BigDecimal {
        val clientInDb = findById(clientId)
        return clientInDb?.let { client ->
            val ownedPassesAfterDate = passRepository.findAllByClientAndPurchasedAtAfter(client, afterDate)
            ownedPassesAfterDate.map { it.purchasedFor ?: BigDecimal.ZERO }.sumOf { it }
        } ?: throw ClientNotFoundException(clientId)
    }

    override fun deleteById(clientId: ObjectId) {
        clientRepository.deleteById(clientId)
    }

    private fun <R> Client.ifUnique(predicate: Client.() -> Boolean, finisher: (Client) -> R): R {
        return if (predicate(this)) finisher.invoke(this) else throw ClientAlreadyExistsException()
    }

}

