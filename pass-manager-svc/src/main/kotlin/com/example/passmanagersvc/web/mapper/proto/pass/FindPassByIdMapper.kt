package com.example.passmanagersvc.web.mapper.proto.pass

import com.example.internal.commonmodels.Error
import com.example.internal.commonmodels.Pass
import com.example.internal.input.reqreply.FindPassByIdResponse
import com.example.passmanagersvc.domain.MongoPass
import com.example.passmanagersvc.exception.PassNotFoundException
import com.example.passmanagersvc.web.mapper.proto.DecimalProtoMapper.toBDecimal

object FindPassByIdMapper {
    fun MongoPass.toProto(): Pass {
        return Pass.newBuilder()
            .setPassOwnerId(passOwnerId.toString())
            .setPassTypeId(passTypeId.toString())
            .setPurchasedFor(purchasedFor?.toBDecimal())
            .build()
    }

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
