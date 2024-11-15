package com.example.passmanagersvc.infrastructure.nats.mapper

import com.example.commonmodels.Error
import com.example.commonmodels.Pass
import com.example.core.exception.PassNotFoundException
import com.example.internal.input.reqreply.FindPassByIdResponse

object FindPassByIdMapper {
    fun Pass.toSuccessFindPassByIdResponse(): FindPassByIdResponse {
        return FindPassByIdResponse.newBuilder().also {
            it.successBuilder.setPass(this)
        }.build()
    }

    fun failureFindByIdPassResponse(throwable: Throwable): FindPassByIdResponse {
        return FindPassByIdResponse.newBuilder().apply {
            failureBuilder.setMessage(throwable.message)
            when (throwable) {
                is PassNotFoundException -> failureBuilder.setNotFoundById(Error.getDefaultInstance())
            }
        }.build()
    }
}
