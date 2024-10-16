package com.example.passmanagersvc.web.mapper.proto.pass

import com.example.internal.commonmodels.Error
import com.example.internal.commonmodels.Pass
import com.example.internal.input.reqreply.CreatePassRequest
import com.example.internal.input.reqreply.CreatePassResponse
import com.example.passmanagersvc.domain.MongoPass
import com.example.passmanagersvc.exception.PassOwnerNotFoundException
import com.example.passmanagersvc.exception.PassTypeNotFoundException
import com.example.passmanagersvc.web.mapper.proto.DecimalProtoMapper.toBigDecimal
import org.bson.types.ObjectId

object CreatePassMapper {
    fun CreatePassRequest.toModel(): MongoPass {
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
                is PassTypeNotFoundException -> failureBuilder.setPassTypeNotFoundId(Error.newBuilder())
                is PassOwnerNotFoundException -> failureBuilder.setOwnerNotFoundById(Error.newBuilder())
            }
        }.build()
    }
}
