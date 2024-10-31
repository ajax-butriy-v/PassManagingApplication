package com.example.passmanagersvc.web.mapper.proto.pass

import com.example.commonmodel.Error
import com.example.commonmodel.Pass
import com.example.core.exception.PassOwnerNotFoundException
import com.example.core.exception.PassTypeNotFoundException
import com.example.core.web.mapper.proto.DecimalProtoMapper.toBigDecimal
import com.example.internal.input.reqreply.CreatePassResponse
import com.example.passmanagersvc.domain.MongoPass
import org.bson.types.ObjectId

object CreatePassMapper {
    fun Pass.toModel(): MongoPass {
        return MongoPass(
            id = null,
            purchasedFor = purchasedFor.toBigDecimal(),
            passOwnerId = ObjectId(passOwnerId),
            passTypeId = ObjectId(passTypeId)
        )
    }

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
