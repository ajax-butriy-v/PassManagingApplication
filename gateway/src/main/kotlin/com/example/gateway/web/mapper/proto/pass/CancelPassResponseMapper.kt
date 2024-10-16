package com.example.gateway.web.mapper.proto.pass

import com.example.internal.input.reqreply.CancelPassResponse
import com.example.passmanagersvc.exception.PassOwnerNotFoundException

object CancelPassResponseMapper {
    fun CancelPassResponse.toUnitResponse() {
        require(this != CancelPassResponse.getDefaultInstance()) {
            "Response must not be default instance."
        }

        if (hasFailure()) {
            val message = failure.message.orEmpty()
            when (failure.errorCase!!) {
                CancelPassResponse.Failure.ErrorCase.PASS_OWNER_NOT_FOUND_BY_ID -> throw PassOwnerNotFoundException(
                    message
                )

                CancelPassResponse.Failure.ErrorCase.ERROR_NOT_SET -> error(message)
            }
        }
    }
}
