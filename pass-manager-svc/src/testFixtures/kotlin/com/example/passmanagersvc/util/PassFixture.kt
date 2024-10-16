package com.example.passmanagersvc.util

import com.example.passmanagersvc.domain.MongoPass
import com.example.passmanagersvc.domain.MongoPassType
import com.example.passmanagersvc.util.PassOwnerFixture.passOwnerFromDb
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit


object PassFixture {
    private val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    private val instant = Instant.now(clock).truncatedTo(ChronoUnit.MILLIS)

    private val passTypesToCreate = listOf("First type", "Second type", "Third type")
        .map {
            MongoPassType(
                id = null,
                activeFrom = instant,
                activeTo = instant.plus(10, ChronoUnit.DAYS),
                name = it,
                price = BigDecimal.TEN
            )
        }
    val passTypeToCreate = passTypesToCreate.first()
    val passTypes = passTypesToCreate.map { it.copy(id = ObjectId.get()) }
    val singlePassType = passTypes.first()
    val singlePassTypeId = singlePassType.id!!.toString()

    val passesToCreate = passTypes.map {
        MongoPass(
            id = null,
            purchasedFor = BigDecimal.TEN,
            passOwnerId = passOwnerFromDb.id,
            passTypeId = it.id,
            purchasedAt = instant,
        )
    }
    val passesFromDb = passesToCreate.map { it.copy(id = ObjectId.get()) }
    val passToCreate = passesToCreate.first()
    val passFromDb = passesFromDb.first()
    val singlePassId = passFromDb.id.toString()
}

