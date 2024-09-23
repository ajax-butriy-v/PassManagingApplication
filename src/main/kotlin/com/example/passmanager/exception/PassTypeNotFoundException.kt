package com.example.passmanager.exception

internal class PassTypeNotFoundException(passOwnerId: String) :
    RuntimeException("Could not find pass type by id $passOwnerId")
