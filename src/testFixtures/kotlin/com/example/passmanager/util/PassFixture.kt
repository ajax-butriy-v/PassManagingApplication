package com.example.passmanager.util

import com.example.passmanager.domain.MongoPass
import com.example.passmanager.domain.MongoPassType
import com.example.passmanager.util.PassOwnerFixture.passOwnerFromDb
import com.example.passmanager.web.dto.PassDto
import com.example.passmanager.web.mapper.PassMapper.toDto
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit


object PassFixture {
    private val clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
    private val instant = clock.instant()

    val passTypesToCreate = listOf("First type", "Second type", "Third type")
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

    val dto = passFromDb.toDto()
    val dtoWithInvalidIdFormats = PassDto(
        purchasedFor = BigDecimal.TEN,
        passOwnerId = "not valid",
        passTypeId = "not valid"
    )
    val dtoWithValidIdFormats = passFromDb.copy(passOwnerId = ObjectId.get(), passTypeId = ObjectId.get()).toDto()
}

