package com.example.passmanager.web

import com.example.passmanager.exception.InvalidIdTypeException
import com.example.passmanager.web.dto.ExceptionResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ResponseEntityControllerAdvice {

    @ExceptionHandler(InvalidIdTypeException::class)
    fun handleBadRequest(runtimeException: RuntimeException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.badRequest().body(ExceptionResponse(runtimeException.message ?: ""))
    }
}
