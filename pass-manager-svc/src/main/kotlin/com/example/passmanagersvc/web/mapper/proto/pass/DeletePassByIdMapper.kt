package com.example.passmanagersvc.web.mapper.proto.pass

import com.example.passmanagersvc.input.reqreply.DeletePassByIdResponse

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
