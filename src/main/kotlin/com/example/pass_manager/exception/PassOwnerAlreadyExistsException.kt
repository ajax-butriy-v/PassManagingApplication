package com.example.pass_manager.exception

class PassOwnerAlreadyExistsException(
    message: String = "Pass owner with such credentials already exists.",
) : RuntimeException(message)

