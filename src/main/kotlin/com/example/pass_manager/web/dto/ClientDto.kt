package com.example.pass_manager.web.dto

import jakarta.validation.constraints.NotBlank

data class ClientDto(
    @NotBlank(message = "Specify first name")
    val firstName: String?,
    @NotBlank(message = "Specify last name")
    val lastName: String?,
    @NotBlank(message = "Specify phone number")
    val phoneNumber: String?,
    @NotBlank(message = "Specify email")
    val email: String?,
)
