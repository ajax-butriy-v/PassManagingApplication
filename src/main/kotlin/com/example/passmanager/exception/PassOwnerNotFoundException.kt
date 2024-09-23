package com.example.passmanager.exception

internal class PassOwnerNotFoundException(passOwnerId: String) :
    RuntimeException("Could not find pass owner by id $passOwnerId")
