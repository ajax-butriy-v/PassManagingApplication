package com.example.passmanagersvc.infrastructure.mongo.mapper

import com.example.passmanagersvc.domain.PassType
import com.example.passmanagersvc.infrastructure.mongo.entity.MongoPassType
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.Instant

object PassTypeMapper {
    fun PassType.toModel(): MongoPassType {
        return MongoPassType(
            id = id?.let { ObjectId(it) },
            activeFrom = activeFrom,
            activeTo = activeTo,
            name = name,
            price = price,
            version = version ?: 0L
        )
    }

    fun MongoPassType.toDomain(): PassType {
        return PassType(
            id = id.toString(),
            activeFrom = activeFrom,
            activeTo = activeTo ?: Instant.now(),
            name = name.orEmpty(),
            price = price ?: BigDecimal.ZERO,
            version = version
        )
    }
}