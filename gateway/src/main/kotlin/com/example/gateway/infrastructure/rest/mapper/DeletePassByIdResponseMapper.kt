package com.example.gateway.infrastructure.rest.mapper

import com.example.core.exception.InternalRuntimeException
import com.example.internal.input.reqreply.DeletePassByIdResponse

object DeletePassByIdResponseMapper {
    fun DeletePassByIdResponse.toDeleteResponse() {
        when (responseCase!!) {
            DeletePassByIdResponse.ResponseCase.SUCCESS -> Unit
            DeletePassByIdResponse.ResponseCase.FAILURE -> throw InternalRuntimeException(failure.message.orEmpty())
            DeletePassByIdResponse.ResponseCase.RESPONSE_NOT_SET -> throw InternalRuntimeException()
        }
    }
}
