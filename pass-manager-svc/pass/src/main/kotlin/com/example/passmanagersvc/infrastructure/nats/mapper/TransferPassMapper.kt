package com.example.passmanagersvc.infrastructure.nats.mapper

import com.example.commonmodels.Error
import com.example.core.exception.PassNotFoundException
import com.example.core.exception.PassOwnerNotFoundException
import com.example.internal.input.reqreply.TransferPassResponse

object TransferPassMapper {
    fun successTransferPassResponse(): TransferPassResponse {
        return TransferPassResponse.newBuilder().apply {
            successBuilder
        }.build()
    }

    fun failureTransferPassResponse(throwable: Throwable): TransferPassResponse {
        return TransferPassResponse.newBuilder().apply {
            failureBuilder.setMessage(throwable.message.orEmpty())
            when (throwable) {
                is PassNotFoundException -> failureBuilder.setPassNotFoundById(Error.getDefaultInstance())
                is PassOwnerNotFoundException -> failureBuilder.setPassOwnerNotFoundById(Error.getDefaultInstance())
            }
        }.build()
    }
}
