package com.example.passmanager.util

import com.example.passmanager.domain.MongoPass
import com.example.passmanager.domain.MongoPassType
import com.example.passmanager.util.PassOwnerFixture.passOwnerFromDb
import com.example.passmanager.web.dto.PassDto
import com.example.passmanager.web.mapper.PassMapper.toDto
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.Instant

object PassFixture {
    private val passTypes = listOf("First type", "Second type", "Third type")
        .map {
            MongoPassType(
                id = ObjectId.get(),
                activeFrom = Instant.MIN,
                activeTo = Instant.MAX,
                name = it,
                price = BigDecimal.TEN
            )
        }
    val singlePassType = passTypes.first()
    val singlePassTypeId = singlePassType.id!!.toString()
    val passToCreate = MongoPass(
        id = null,
        purchasedFor = BigDecimal.TEN,
        passOwner = passOwnerFromDb,
        passType = null,
        purchasedAt = Instant.now(),
    )

    val passes = passTypes.map {
        MongoPass(
            id = ObjectId.get(),
            purchasedFor = BigDecimal.TEN,
            passOwner = passOwnerFromDb,
            passType = it,
            purchasedAt = Instant.now(),
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

