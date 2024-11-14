package com.example.passmanagersvc.infrastructure.rest.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class PassOwnerDto(
    @field:NotBlank(message = "Specify first name")
    val firstName: String?,
    @field:NotBlank(message = "Specify last name")
    val lastName: String?,
    @field:Pattern(regexp = "(^$|[0-9]{10})", message = "Specify valid phone number")
    val phoneNumber: String?,
    @field:Email(message = "Specify valid email")
    val email: String?,
)
