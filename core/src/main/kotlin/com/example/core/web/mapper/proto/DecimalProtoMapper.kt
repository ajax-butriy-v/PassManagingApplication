package com.example.core.web.mapper.proto

import com.example.commonmodels.BDecimal
import com.google.protobuf.ByteString
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext

object DecimalProtoMapper {
    fun BigDecimal.toBDecimal(): BDecimal {
        return BDecimal.newBuilder()
            .setScale(scale())
            .setPrecision(precision())
            .setValue(ByteString.copyFrom(unscaledValue().toByteArray()))
            .build()
    }

    fun BDecimal.toBigDecimal(): BigDecimal {
        return BigDecimal(BigInteger(value.toByteArray()), scale, MathContext(precision))
    }
}
