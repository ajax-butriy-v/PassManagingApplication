package com.example.pass_manager.web.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

class PassDto(
    @field:Positive(message = "Specify amount of money spent")
    val purchasedFor: BigDecimal?,
    @field:NotNull(message = "Specify client id")
    val passOwnerId: String?,
    @field:NotNull(message = "Specify pass type id")
    val passTypeId: String?,
)

