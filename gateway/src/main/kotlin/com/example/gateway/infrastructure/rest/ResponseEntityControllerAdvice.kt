package com.example.gateway.infrastructure.rest

import com.example.core.exception.InvalidObjectIdFormatException
import com.example.core.exception.PassNotFoundException
import com.example.core.exception.PassOwnerNotFoundException
import com.example.core.exception.PassTypeNotFoundException
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestControllerAdvice
internal object ResponseEntityControllerAdvice {

    @ExceptionHandler(InvalidObjectIdFormatException::class)
    fun handleBadRequest(runtimeException: RuntimeException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.badRequest().body(ExceptionResponse(runtimeException.message.orEmpty()))
    }

    @ExceptionHandler(
        value = [PassTypeNotFoundException::class, PassOwnerNotFoundException::class, PassNotFoundException::class]
    )
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
