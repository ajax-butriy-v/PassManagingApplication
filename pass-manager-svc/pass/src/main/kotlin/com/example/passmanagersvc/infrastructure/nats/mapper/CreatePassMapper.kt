package com.example.passmanagersvc.infrastructure.nats.mapper

import com.example.commonmodels.Error
import com.example.commonmodels.Pass
import com.example.core.exception.PassOwnerNotFoundException
import com.example.core.exception.PassTypeNotFoundException
import com.example.internal.input.reqreply.CreatePassResponse

object CreatePassMapper {

    fun Pass.toSuccessCreatePassResponse(): CreatePassResponse {
        return CreatePassResponse.newBuilder().also {
            it.successBuilder.setPass(this)
        }.build()
    }

    fun failureCreatedPassResponse(throwable: Throwable): CreatePassResponse {
        return CreatePassResponse.newBuilder().apply {
            failureBuilder.setMessage(throwable.message.orEmpty())
            when (throwable) {
                is PassTypeNotFoundException -> failureBuilder.setPassTypeNotFoundId(Error.getDefaultInstance())
                is PassOwnerNotFoundException -> failureBuilder.setOwnerNotFoundById(Error.getDefaultInstance())
            }
        }.build()
    }
}
