package com.example.passmanager.web.mapper

import com.example.passmanager.domain.MongoPass
import com.example.passmanager.web.dto.PassDto
import org.bson.types.ObjectId
import java.math.BigDecimal

internal object PassMapper {
    fun MongoPass.toDto(): PassDto {
        return PassDto(purchasedFor ?: BigDecimal.ZERO, passOwnerId.toString(), passTypeId.toString())
    }

    fun PassDto.toEntity(): MongoPass {
        return MongoPass(
            id = null,
            purchasedFor = purchasedFor,
            passOwnerId = ObjectId(passOwnerId),
            passTypeId = ObjectId(passTypeId)
        )
    }
}
