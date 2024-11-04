package com.example.passmanagersvc.dto

import java.math.BigDecimal

data class PriceDistribution(
    val typeName: String?,
    val spentForPassType: BigDecimal,
)
