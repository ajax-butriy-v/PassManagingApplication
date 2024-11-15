package com.example.passmanagersvc.domain

import java.math.BigDecimal

data class PriceDistribution(
    val typeName: String,
    val spentForPassType: BigDecimal,
)
