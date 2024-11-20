package com.example.passmanagersvc.passowner.infrastructure.rest

import com.example.core.exception.PassOwnerNotFoundException
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestControllerAdvice
internal object ResponseEntityControllerAdvice {

    @ExceptionHandler(value = [PassOwnerNotFoundException::class])
    fun handleNotFound(runtimeException: RuntimeException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.status(NOT_FOUND).body(ExceptionResponse(runtimeException.message.orEmpty()))
    }

    data class ExceptionResponse(val message: String, val timeStamp: String) {
        constructor(message: String) : this(
            message,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"))
        )
    }
}
