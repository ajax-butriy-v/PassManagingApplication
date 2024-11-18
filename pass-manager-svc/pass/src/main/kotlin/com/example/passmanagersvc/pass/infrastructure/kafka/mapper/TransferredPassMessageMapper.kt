package com.example.passmanagersvc.pass.infrastructure.kafka.mapper

import com.example.internal.input.reqreply.TransferredPassMessage
import com.example.passmanagersvc.pass.domain.Pass
import com.example.passmanagersvc.pass.infrastructure.nats.mapper.ProtoPassMapper.toProto

object TransferredPassMessageMapper {

    fun Pass.toTransferredPassMessage(previousPassOwnerId: String): TransferredPassMessage {
        return TransferredPassMessage.newBuilder().also {
            it.pass = toProto()
            it.previousPassOwnerId = previousPassOwnerId
            it.passId = id.toString()
        }.build()
    }
}
