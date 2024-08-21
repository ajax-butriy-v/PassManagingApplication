package com.example.pass_manager.web.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.Instant

class PassDto(
    @Positive(message = "Specify amount of money spent")
    val purchasedFor: BigDecimal?,
    @NotNull(message = "Specify client id")
    val passOwnerId: String?,
    @NotNull(message = "Specify pass type id")
    val passTypeId: String?,
    @PastOrPresent(message = "Specify valid purchased time")
    val purchasedAt: Instant?,
)
