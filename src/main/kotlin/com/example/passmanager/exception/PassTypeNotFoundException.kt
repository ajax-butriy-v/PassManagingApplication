package com.example.passmanager.exception

internal class PassTypeNotFoundException(passTypeId: String) :
    RuntimeException("Could not find pass type by id $passTypeId")
