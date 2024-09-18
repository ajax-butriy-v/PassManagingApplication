package com.example.passmanager.repositories

import com.example.passmanager.domain.MongoPass
import com.example.passmanager.web.dto.PriceDistribution
import java.math.BigDecimal
import java.time.LocalDate

interface PassRepository {
    fun findByOwnerAndPurchasedAfter(passOwnerId: String, afterDate: LocalDate): List<MongoPass>
    fun findAllByPassOwnerId(passOwnerId: String): List<MongoPass>
    fun findById(passId: String): MongoPass?
    fun insert(newPass: MongoPass): MongoPass
    fun save(pass: MongoPass): MongoPass
    fun deleteById(passId: String)
    fun deleteAllByOwnerId(passOwnerId: String)
    fun deleteByIdAndOwnerId(passId: String, passOwnerId: String): Boolean
    fun sumPurchasedAtAfterDate(passOwnerId: String, afterDate: LocalDate): BigDecimal
    fun getPassesPriceDistribution(passOwnerId: String): List<PriceDistribution>
}
