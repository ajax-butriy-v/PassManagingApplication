package com.example.gateway.mapper.rest

import com.example.core.exception.InternalRuntimeException
import com.example.core.exception.PassOwnerNotFoundException
import com.example.internal.input.reqreply.CancelPassResponse

object CancelPassResponseMapper {
    fun CancelPassResponse.toUnitResponse() {
        when (responseCase!!) {
            CancelPassResponse.ResponseCase.SUCCESS -> Unit
            CancelPassResponse.ResponseCase.FAILURE -> failureCase()
            CancelPassResponse.ResponseCase.RESPONSE_NOT_SET -> throw InternalRuntimeException()
        }
    }

    private fun CancelPassResponse.failureCase() {
        val message = failure.message.orEmpty()
        when (failure.errorCase!!) {
            CancelPassResponse.Failure.ErrorCase.PASS_OWNER_NOT_FOUND_BY_ID -> throw PassOwnerNotFoundException(
                message
            )

            CancelPassResponse.Failure.ErrorCase.ERROR_NOT_SET -> throw InternalRuntimeException(message)
        }
    }
}
