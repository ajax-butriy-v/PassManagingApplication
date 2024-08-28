package com.example.passmanager.exception

class PassOwnerAlreadyExistsException(
    message: String = "Pass owner with such credentials already exists.",
) : RuntimeException(message)

