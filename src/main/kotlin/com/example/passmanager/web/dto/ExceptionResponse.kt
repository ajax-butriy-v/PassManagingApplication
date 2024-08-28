package com.example.passmanager.web.dto

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ExceptionResponse(val message: String, val timeStamp: String) {
    constructor(message: String) : this(
        message, LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"))
    )
}

