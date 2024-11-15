package com.example.passmanagersvc.infrastructure.kafka.mapper

import com.example.core.web.mapper.proto.DecimalProtoMapper.toBDecimal
import com.example.core.web.mapper.proto.DecimalProtoMapper.toBigDecimal
import com.example.internal.input.reqreply.TransferredPassMessage
import com.example.passmanagersvc.domain.Pass
import java.time.Instant
import com.example.commonmodels.Pass as ProtoPass

object TransferredPassMessageMapper {

    fun Pass.toTransferredPassMessage(previousPassOwnerId: String): TransferredPassMessage {
        return TransferredPassMessage.newBuilder().also {
            it.pass = toProto()
            it.previousPassOwnerId = previousPassOwnerId
            it.passId = id.toString()
        }.build()
    }

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
