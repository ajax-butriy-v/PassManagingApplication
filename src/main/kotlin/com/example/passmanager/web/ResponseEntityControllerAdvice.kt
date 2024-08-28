package com.example.passmanager.web

import com.example.passmanager.exception.InvalidIdTypeException
import com.example.passmanager.exception.PassOwnerNotFoundException
import com.example.passmanager.exception.PassTypeNotFoundException
import com.example.passmanager.web.dto.ExceptionResponse
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ResponseEntityControllerAdvice {

    @ExceptionHandler(InvalidIdTypeException::class)
    fun handleBadRequest(runtimeException: RuntimeException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.badRequest().body(ExceptionResponse(runtimeException.message ?: ""))
    }

    @ExceptionHandler(
        value = [PassTypeNotFoundException::class, PassOwnerNotFoundException::class, PassTypeNotFoundException::class]
    )
    fun handleNotFound(runtimeException: RuntimeException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.status(NOT_FOUND).body(ExceptionResponse(runtimeException.message ?: ""))
    }
}

