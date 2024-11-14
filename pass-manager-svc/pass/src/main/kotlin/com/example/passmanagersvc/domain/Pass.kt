package com.example.passmanagersvc.domain

import java.math.BigDecimal
import java.time.Instant

data class Pass(
    val id: String?,
    val purchasedFor: BigDecimal,
    val passOwnerId: String,
    val passTypeId: String,
    val purchasedAt: Instant,
    val version: Long?,
)