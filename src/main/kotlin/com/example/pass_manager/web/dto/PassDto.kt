package com.example.pass_manager.web.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

class PassDto(
    @Positive(message = "Specify amount of money spent")
    val purchasedFor: BigDecimal?,
    @NotNull(message = "Specify client id")
    val passOwnerId: String?,
    @NotNull(message = "Specify pass type id")
    val passTypeId: String?,
)
