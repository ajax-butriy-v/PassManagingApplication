package com.example.passmanagersvc.web.mapper.proto.pass

import com.example.internal.commonmodels.Error
import com.example.internal.input.reqreply.TransferPassResponse
import com.example.passmanagersvc.exception.PassNotFoundException
import com.example.passmanagersvc.exception.PassOwnerNotFoundException

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
                is PassNotFoundException -> failureBuilder.setPassNotFoundById(Error.newBuilder())
                is PassOwnerNotFoundException -> failureBuilder.setPassOwnerNotFoundById(Error.newBuilder())
            }
        }.build()
    }
}
