package com.example.gateway.util

import com.example.gateway.web.dto.PassDto
import org.bson.types.ObjectId
import java.math.BigDecimal

object PassDtoFixture {
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

    val passId = ObjectId.get().toString()
}
