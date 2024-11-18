package com.example.passmanagersvc.passowner.infrastructure.rest.dto

data class PassOwnerUpdateDto(
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String?,
    val email: String?,
)
