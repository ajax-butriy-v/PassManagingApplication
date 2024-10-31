package com.example.gateway.mapper.grpc

import com.example.commonmodel.Pass
import com.example.core.exception.InternalRuntimeException
import com.example.core.exception.PassNotFoundException
import com.example.grpcapi.reqrep.pass.FindPassByIdRequest
import com.example.grpcapi.reqrep.pass.FindPassByIdResponse
import com.example.internal.input.reqreply.FindPassByIdResponse.Failure.ErrorCase.ERROR_NOT_SET
import com.example.internal.input.reqreply.FindPassByIdResponse.Failure.ErrorCase.NOT_FOUND_BY_ID
import com.example.internal.input.reqreply.FindPassByIdResponse.ResponseCase.FAILURE
import com.example.internal.input.reqreply.FindPassByIdResponse.ResponseCase.RESPONSE_NOT_SET
import com.example.internal.input.reqreply.FindPassByIdResponse.ResponseCase.SUCCESS
import com.example.internal.input.reqreply.FindPassByIdRequest as InternalFindPassByIdRequest
import com.example.internal.input.reqreply.FindPassByIdResponse as InternalFindPassByIdResponse

object FindPassByIdMapper {

    fun FindPassByIdRequest.toInternalProto(): InternalFindPassByIdRequest {
        return InternalFindPassByIdRequest.newBuilder()
            .setId(id)
            .build()
    }

    fun InternalFindPassByIdResponse.toGrpcProto(): FindPassByIdResponse {
        when (this.responseCase!!) {
            SUCCESS -> return success.pass.toFindPassByIdResponse()
            FAILURE -> failureCase()
            RESPONSE_NOT_SET -> throw InternalRuntimeException()
        }
    }

    private fun Pass.toFindPassByIdResponse(): FindPassByIdResponse {
        return FindPassByIdResponse.newBuilder().also {
            it.successBuilder.setPass(this)
        }.build()
    }

    private fun InternalFindPassByIdResponse.failureCase(): Nothing {
        val message = failure.message.orEmpty()
        when (failure.errorCase!!) {
            NOT_FOUND_BY_ID -> throw PassNotFoundException(message)
            ERROR_NOT_SET -> throw InternalRuntimeException(message)
        }
    }
}
