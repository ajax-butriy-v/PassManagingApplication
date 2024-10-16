package com.example.gateway.web

import com.example.gateway.exception.InvalidObjectIdFormatException
import com.example.passmanagersvc.exception.PassNotFoundException
import com.example.passmanagersvc.exception.PassOwnerNotFoundException
import com.example.passmanagersvc.exception.PassTypeNotFoundException
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
