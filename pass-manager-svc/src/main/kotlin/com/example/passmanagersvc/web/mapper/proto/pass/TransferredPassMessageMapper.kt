package com.example.passmanagersvc.web.mapper.proto.pass

import com.example.internal.input.reqreply.TransferredPassMessage
import com.example.passmanagersvc.domain.MongoPass
import com.example.passmanagersvc.web.mapper.proto.pass.FindPassByIdMapper.toProto

object TransferredPassMessageMapper {

    fun MongoPass.toTransferredPassMessage(previousPassOwnerId: String): TransferredPassMessage {
        return TransferredPassMessage.newBuilder().also {
            it.pass = toProto()
            it.previousPassOwnerId = previousPassOwnerId
            it.passId = id.toString()
        }.build()
    }
}
