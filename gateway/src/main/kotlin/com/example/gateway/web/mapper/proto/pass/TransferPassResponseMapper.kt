package com.example.gateway.web.mapper.proto.pass

import com.example.internal.input.reqreply.TransferPassResponse
import com.example.passmanagersvc.exception.PassNotFoundException
import com.example.passmanagersvc.exception.PassOwnerNotFoundException

object TransferPassResponseMapper {
    fun TransferPassResponse.toTransferResponse() {
        require(this != TransferPassResponse.getDefaultInstance()) {
            "Response must not be default instance."
        }

        if (hasFailure()) {
            val message = failure.message.orEmpty()
            when (failure.errorCase!!) {
                TransferPassResponse.Failure.ErrorCase.PASS_OWNER_NOT_FOUND_BY_ID -> throw PassOwnerNotFoundException(
                    message
                )

                TransferPassResponse.Failure.ErrorCase.PASS_NOT_FOUND_BY_ID -> throw PassNotFoundException(message)
                TransferPassResponse.Failure.ErrorCase.ERROR_NOT_SET -> error(message)
            }
        }
    }
}
