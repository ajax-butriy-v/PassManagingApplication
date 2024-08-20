package com.example.pass_manager.web.dto

import java.math.BigDecimal

data class PriceDistribution(
    val typeName: String?,
    val spentForPassType: BigDecimal,
)
