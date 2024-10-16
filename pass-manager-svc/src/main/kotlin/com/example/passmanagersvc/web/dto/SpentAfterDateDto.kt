package com.example.passmanagersvc.web.dto

import java.math.BigDecimal
import java.time.LocalDate

data class SpentAfterDateDto(
    val passOwnerId: String,
    val afterDate: LocalDate,
    val total: BigDecimal,
)
