package com.example.passmanagersvc.pass.infrastructure.nats.mapper

import com.example.core.mapper.proto.DecimalProtoMapper.toBDecimal
import com.example.core.mapper.proto.DecimalProtoMapper.toBigDecimal
import com.example.passmanagersvc.pass.domain.Pass
import java.time.Instant
import com.example.commonmodels.Pass as ProtoPass

object ProtoPassMapper {
    fun Pass.toProto(): ProtoPass {
        return ProtoPass.newBuilder()
            .setPassOwnerId(passOwnerId)
            .setPassTypeId(passTypeId)
            .setPurchasedFor(purchasedFor.toBDecimal())
            .build()
    }

    fun ProtoPass.toDomain(): Pass {
        return Pass(
            id = null,
            purchasedFor = purchasedFor.toBigDecimal(),
            passOwnerId = passOwnerId,
            passTypeId = passTypeId,
            purchasedAt = Instant.now(),
            version = 0L
        )
    }
}
