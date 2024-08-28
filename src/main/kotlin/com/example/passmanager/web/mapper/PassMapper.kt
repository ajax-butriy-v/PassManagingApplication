package com.example.passmanager.web.mapper

import com.example.passmanager.domain.MongoPass
import com.example.passmanager.web.dto.PassDto
import java.math.BigDecimal

object PassMapper {
    fun MongoPass.toDto(): PassDto {
        return PassDto(purchasedFor ?: BigDecimal.ZERO, passOwner?.id.toString(), passType?.id.toString())
    }

    fun PassDto.toEntity(): MongoPass {
        return MongoPass(
            id = null,
            purchasedFor = purchasedFor,
            passOwner = null,
            passType = null
        )
    }
}
