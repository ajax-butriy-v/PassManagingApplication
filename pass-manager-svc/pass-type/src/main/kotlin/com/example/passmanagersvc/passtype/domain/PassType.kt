package com.example.passmanagersvc.passtype.domain

import java.math.BigDecimal
import java.time.Instant

data class PassType(
    val id: String?,
    val activeFrom: Instant?,
    val activeTo: Instant,
    val name: String,
    val price: BigDecimal,
    val version: Long?,
)
