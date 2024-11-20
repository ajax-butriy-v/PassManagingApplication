package com.example.passmanagersvc.pass.infrastructure.nats.mapper

import com.example.internal.input.reqreply.DeletePassByIdResponse

object DeletePassByIdMapper {
    fun successDeletePassByIdResponse(): DeletePassByIdResponse {
        return DeletePassByIdResponse.newBuilder().apply {
            successBuilder
        }.build()
    }

    fun failureDeletePassByIdResponse(throwable: Throwable): DeletePassByIdResponse {
        val message = throwable.message.orEmpty()
        return DeletePassByIdResponse.newBuilder().apply {
            failureBuilder.setMessage(message)
        }.build()
    }
}
