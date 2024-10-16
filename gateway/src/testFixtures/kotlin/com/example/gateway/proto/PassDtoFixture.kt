package com.example.gateway.proto

import com.example.gateway.web.dto.PassDto
import com.example.passmanagersvc.domain.MongoPass
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object PassDtoFixture {
    private val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    private val instant = Instant.now(clock).truncatedTo(ChronoUnit.MILLIS)
    val passOwnerId = ObjectId.get()
    val passTypeId = ObjectId.get().toString()

    val passDtoWithInvalidIdFormats = PassDto(
        passOwnerId = "not valid",
        passTypeId = "not valid",
        purchasedFor = BigDecimal.TEN
    )

    val passDto = PassDto(
        purchasedFor = BigDecimal.TEN,
        passOwnerId = ObjectId.get().toString(),
        passTypeId = ObjectId.get().toString()
    )

    val passFromDto = MongoPass(
        id = ObjectId.get(),
        purchasedFor = passDto.purchasedFor,
        passOwnerId = ObjectId(passDto.passOwnerId),
        passTypeId = ObjectId(passDto.passTypeId),
        purchasedAt = instant,
    )
}