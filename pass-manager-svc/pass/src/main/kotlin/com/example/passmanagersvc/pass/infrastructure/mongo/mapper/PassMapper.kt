package com.example.passmanagersvc.pass.infrastructure.mongo.mapper

import com.example.passmanagersvc.pass.domain.Pass
import com.example.passmanagersvc.pass.infrastructure.mongo.entity.MongoPass
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.Instant

object PassMapper {
    fun Pass.toModel(): MongoPass {
        return MongoPass(
            id = id?.let { ObjectId(it) },
            purchasedFor = purchasedFor,
            passOwnerId = ObjectId(passOwnerId),
            passTypeId = ObjectId(passTypeId),
            purchasedAt = purchasedAt,
            version = version ?: 0L
        )
    }

    fun MongoPass.toDomain(): Pass {
        return Pass(
            id = id?.toString(),
            purchasedFor = purchasedFor ?: BigDecimal.ZERO,
            passOwnerId = passOwnerId.toString(),
            passTypeId = passTypeId.toString(),
            purchasedAt = purchasedAt ?: Instant.now(),
            version = version
        )
    }
}
