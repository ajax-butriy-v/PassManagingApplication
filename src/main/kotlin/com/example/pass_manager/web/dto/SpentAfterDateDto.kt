package com.example.pass_manager.web.dto

import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.Instant

data class SpentAfterDateDto(
    val afterDate: Instant,
    val clientId: ObjectId,
    val total: BigDecimal
)
