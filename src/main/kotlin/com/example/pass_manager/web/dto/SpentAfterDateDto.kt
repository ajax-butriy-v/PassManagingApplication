package com.example.pass_manager.web.dto

import java.math.BigDecimal
import java.time.Instant

data class SpentAfterDateDto(
    val passOwnerId: String,
    val afterDate: Instant,
    val total: BigDecimal,
)

