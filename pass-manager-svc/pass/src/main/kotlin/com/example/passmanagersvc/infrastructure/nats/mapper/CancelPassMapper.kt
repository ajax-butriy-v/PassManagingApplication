package com.example.passmanagersvc.infrastructure.nats.mapper

import com.example.commonmodels.Error
import com.example.core.exception.PassOwnerNotFoundException
import com.example.internal.input.reqreply.CancelPassResponse

object CancelPassMapper {
    fun successCancelPassResponse(): CancelPassResponse {
        return CancelPassResponse.newBuilder().apply {
            successBuilder
        }.build()
    }

    fun failureCancelPassResponse(throwable: Throwable): CancelPassResponse {
        return CancelPassResponse.newBuilder().apply {
            failureBuilder.setMessage(throwable.message)
            when (throwable) {
                is PassOwnerNotFoundException -> failureBuilder.setPassOwnerNotFoundById(Error.getDefaultInstance())
            }
        }.build()
    }
}
