package com.example.passmanager.web.dto

import com.example.passmanager.configuration.ValidObjectIdFormat
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class PassDto(
    @field:Positive(message = "Specify amount of money spent")
    val purchasedFor: BigDecimal,
    @ValidObjectIdFormat
    @field:NotNull(message = "Specify client id")
    val passOwnerId: String,
    @ValidObjectIdFormat
    @field:NotNull(message = "Specify pass type id")
    val passTypeId: String,
)
