package com.example.passmanagersvc.web.mapper.proto.pass

import com.example.internal.input.reqreply.TransferredPassStatisticsMessage
import com.example.passmanagersvc.domain.MongoPass
import com.example.passmanagersvc.domain.MongoPassType
import java.time.Duration
import com.google.protobuf.Duration as ProtoDuration

object TransferredPassStatisticsMessageMapper {
    fun MongoPass.toTransferPassStatisticsMessage(
        passType: MongoPassType,
        previousOwnerId: String,
        isDeltaPositive: Boolean,
        durationUntilPassExpiration: Duration,
    ): TransferredPassStatisticsMessage {
        return TransferredPassStatisticsMessage.newBuilder().apply {
            passId = id.toString()
            previousPassOwnerId = previousOwnerId
            passTypeId = passType.id.toString()
            wasPurchasedWithDiscount = isDeltaPositive
            currentPassOwnerId = passOwnerId.toString()
            timeUntilExpiration = durationUntilPassExpiration.toProtoDuration()
        }.build()
    }

    private fun Duration.toProtoDuration(): ProtoDuration {
        return ProtoDuration.newBuilder().also {
            it.seconds = seconds
            it.nanos = nano
        }.build()
    }
}
