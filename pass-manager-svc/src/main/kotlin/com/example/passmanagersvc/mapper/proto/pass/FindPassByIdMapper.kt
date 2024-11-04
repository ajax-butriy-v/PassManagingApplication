package com.example.passmanagersvc.mapper.proto.pass

import com.example.commonmodel.Error
import com.example.commonmodel.Pass
import com.example.core.exception.PassNotFoundException
import com.example.core.web.mapper.proto.DecimalProtoMapper.toBDecimal
import com.example.internal.input.reqreply.FindPassByIdResponse
import com.example.passmanagersvc.domain.MongoPass

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
