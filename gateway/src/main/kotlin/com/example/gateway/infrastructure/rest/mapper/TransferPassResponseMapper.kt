package com.example.gateway.infrastructure.rest.mapper

import com.example.core.exception.InternalRuntimeException
import com.example.core.exception.PassNotFoundException
import com.example.core.exception.PassOwnerNotFoundException
import com.example.internal.input.reqreply.TransferPassResponse

object TransferPassResponseMapper {
    fun TransferPassResponse.toUnitResponse() {
        when (responseCase!!) {
            TransferPassResponse.ResponseCase.SUCCESS -> Unit
            TransferPassResponse.ResponseCase.FAILURE -> failureCase()
            TransferPassResponse.ResponseCase.RESPONSE_NOT_SET -> throw InternalRuntimeException()
        }
    }

    private fun TransferPassResponse.failureCase() {
        val message = failure.message.orEmpty()
        when (failure.errorCase!!) {
            TransferPassResponse.Failure.ErrorCase.PASS_OWNER_NOT_FOUND_BY_ID -> throw PassOwnerNotFoundException(
                message
            )

            TransferPassResponse.Failure.ErrorCase.PASS_NOT_FOUND_BY_ID -> throw PassNotFoundException(message)
            TransferPassResponse.Failure.ErrorCase.ERROR_NOT_SET -> throw InternalRuntimeException(message)
        }
    }
}
