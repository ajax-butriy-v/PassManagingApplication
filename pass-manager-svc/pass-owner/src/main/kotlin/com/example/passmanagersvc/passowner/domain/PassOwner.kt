package com.example.passmanagersvc.passowner.domain

data class PassOwner(
    val id: String?,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
    val version: Long?,
)
