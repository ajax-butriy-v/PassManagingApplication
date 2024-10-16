package com.example.passmanagersvc.web.mapper.proto.pass

import com.example.internal.commonmodels.Error
import com.example.internal.input.reqreply.CancelPassResponse
import com.example.passmanagersvc.exception.PassOwnerNotFoundException

object CancelPassMapper {
    fun successCancelPassResponse(): CancelPassResponse {
        return CancelPassResponse.newBuilder().apply {
            successBuilder
        }.build()
    }

    fun failureCancelPassResponse(throwable: Throwable): CancelPassResponse {
        return CancelPassResponse.newBuilder().also { responseBuilder ->
            responseBuilder.failureBuilder.apply {
                setMessage(throwable.message)
                when (throwable) {
                    is PassOwnerNotFoundException -> setPassOwnerNotFoundById(Error.getDefaultInstance())
                }
            }
        }.build()
    }
}
