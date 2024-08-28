package com.example.passmanager.web

import com.example.passmanager.exception.InvalidIdTypeException
import com.example.passmanager.exception.PassOwnerNotFoundException
import com.example.passmanager.exception.PassTypeNotFoundException
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestControllerAdvice
object ResponseEntityControllerAdvice {

    @ExceptionHandler(InvalidIdTypeException::class)
    private fun handleBadRequest(runtimeException: RuntimeException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.badRequest().body(ExceptionResponse(runtimeException.message ?: ""))
    }

    @ExceptionHandler(
        value = [PassTypeNotFoundException::class, PassOwnerNotFoundException::class, PassTypeNotFoundException::class]
    )
    private fun handleNotFound(runtimeException: RuntimeException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.status(NOT_FOUND).body(ExceptionResponse(runtimeException.message ?: ""))
    }

    private data class ExceptionResponse(val message: String, val timeStamp: String) {
        constructor(message: String) : this(
            message, LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"))
        )
    }
}

