package com.example.passmanagersvc.infrastructure.kafka.mapper

import com.example.internal.input.reqreply.TransferredPassStatisticsMessage
import com.example.passmanagersvc.domain.Pass
import com.example.passmanagersvc.domain.PassType
import java.time.Duration
import com.google.protobuf.Duration as ProtoDuration

object TransferredPassStatisticsMessageMapper {
    fun Pass.toTransferPassStatisticsMessage(
        passType: PassType,
        previousOwnerId: String,
        isDeltaPositive: Boolean,
        durationUntilPassExpiration: Duration,
    ): TransferredPassStatisticsMessage {
        return TransferredPassStatisticsMessage.newBuilder().apply {
            passId = id.toString()
            previousPassOwnerId = previousOwnerId
            passTypeId = passType.id.toString()
            wasPurchasedWithDiscount = isDeltaPositive
            currentPassOwnerId = passOwnerId
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
