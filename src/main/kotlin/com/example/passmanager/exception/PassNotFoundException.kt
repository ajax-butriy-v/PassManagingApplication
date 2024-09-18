package com.example.passmanager.exception

internal class PassNotFoundException(passId: String) : RuntimeException("Could not find pass by id $passId")
