package com.example.gateway.mapper.grpc

import com.example.commonmodel.Pass
import com.example.core.exception.InternalRuntimeException
import com.example.core.exception.PassOwnerNotFoundException
import com.example.core.exception.PassTypeNotFoundException
import com.example.grpcapi.reqrep.pass.CreatePassRequest
import com.example.grpcapi.reqrep.pass.CreatePassResponse
import com.example.internal.input.reqreply.CreatePassResponse.Failure.ErrorCase.ERROR_NOT_SET
import com.example.internal.input.reqreply.CreatePassResponse.Failure.ErrorCase.OWNER_NOT_FOUND_BY_ID
import com.example.internal.input.reqreply.CreatePassResponse.Failure.ErrorCase.PASS_TYPE_NOT_FOUND_ID
import com.example.internal.input.reqreply.CreatePassResponse.ResponseCase.FAILURE
import com.example.internal.input.reqreply.CreatePassResponse.ResponseCase.RESPONSE_NOT_SET
import com.example.internal.input.reqreply.CreatePassResponse.ResponseCase.SUCCESS
import com.example.internal.input.reqreply.CreatePassRequest as InternalCreatePassRequest
import com.example.internal.input.reqreply.CreatePassResponse as InternalCreatePassResponse

object CreatePassMapper {
    fun CreatePassRequest.toInternalProto(): InternalCreatePassRequest {
        return InternalCreatePassRequest.newBuilder()
            .setPassToCreate(passToCreate)
            .build()
    }

    fun InternalCreatePassResponse.toGrpcProto(): CreatePassResponse {
        when (responseCase!!) {
            SUCCESS -> return success.pass.toCreatePassResponse()
            FAILURE -> failure.asException()
            RESPONSE_NOT_SET -> throw InternalRuntimeException()
        }
    }

    private fun Pass.toCreatePassResponse(): CreatePassResponse {
        return CreatePassResponse.newBuilder().also {
            it.successBuilder.setPass(this)
        }.build()
    }

    private fun InternalCreatePassResponse.Failure.asException(): Nothing {
        val message = message.orEmpty()
        when (errorCase!!) {
            OWNER_NOT_FOUND_BY_ID -> throw PassOwnerNotFoundException(message)
            PASS_TYPE_NOT_FOUND_ID -> throw PassTypeNotFoundException(message)
            ERROR_NOT_SET -> throw InternalRuntimeException(message)
        }
    }
}
