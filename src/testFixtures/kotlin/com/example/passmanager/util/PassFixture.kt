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


object PassFixture {
    private val clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
    private val instant = clock.instant()

    val passTypes = listOf("First type", "Second type", "Third type")
        .map {
            MongoPassType(
                id = ObjectId.get(),
                activeFrom = instant,
                activeTo = instant.plusSeconds(1_000_000),
                name = it,
                price = BigDecimal.TEN
            )
        }
    val singlePassType = passTypes.first()
    val singlePassTypeId = singlePassType.id!!.toString()
    val passToCreate = MongoPass(
        id = null,
        purchasedFor = BigDecimal.TEN,
        passOwnerId = passOwnerFromDb.id,
        passTypeId = singlePassType.id,
        purchasedAt = instant,
    )

    val passes = passTypes.map {
        MongoPass(
            id = ObjectId.get(),
            purchasedFor = BigDecimal.TEN,
            passOwnerId = passOwnerFromDb.id,
            passTypeId = it.id,
            purchasedAt = instant,
        )
    }

    val passFromDb = passes.first()
    val singlePassId = passFromDb.id.toString()

    val dtoWithInvalidIdFormats = PassDto(
        purchasedFor = BigDecimal.TEN,
        passOwnerId = "not valid",
        passTypeId = "not valid"
    )

    val dtoWithValidIdFormats = passFromDb.toDto()
}

