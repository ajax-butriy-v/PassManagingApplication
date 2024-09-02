package com.example.passmanager.web.dto

import java.math.BigDecimal

data class PriceDistribution(
    val typeName: String?,
    val spentForPassType: BigDecimal,
)
