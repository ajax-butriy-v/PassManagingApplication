package com.example.passmanager.web.dto

import com.example.passmanager.configuration.ValidIdFormat
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

@ValidIdFormat
class PassDto(
    @field:Positive(message = "Specify amount of money spent")
    val purchasedFor: BigDecimal,
    @field:NotNull(message = "Specify client id")
    val passOwnerId: String,
    @field:NotNull(message = "Specify pass type id")
    val passTypeId: String,
)

