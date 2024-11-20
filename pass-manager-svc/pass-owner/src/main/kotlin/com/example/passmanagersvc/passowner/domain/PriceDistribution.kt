package com.example.passmanagersvc.passowner.domain

import java.math.BigDecimal

data class PriceDistribution(
    val typeName: String,
    val spentForPassType: BigDecimal,
)
