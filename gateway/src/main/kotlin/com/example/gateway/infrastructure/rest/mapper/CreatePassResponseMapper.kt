package com.example.gateway.infrastructure.rest.mapper

import com.example.core.exception.InternalRuntimeException
import com.example.core.exception.PassOwnerNotFoundException
import com.example.core.exception.PassTypeNotFoundException
import com.example.gateway.infrastructure.rest.dto.PassDto
import com.example.gateway.infrastructure.rest.mapper.FindPassByIdResponseMapper.fromProtoToDto
import com.example.gateway.infrastructure.rest.mapper.FindPassByIdResponseMapper.toProto
import com.example.internal.input.reqreply.CreatePassRequest
import com.example.internal.input.reqreply.CreatePassResponse
import com.example.internal.input.reqreply.CreatePassResponse.Failure.ErrorCase.OWNER_NOT_FOUND_BY_ID
import com.example.internal.input.reqreply.CreatePassResponse.Failure.ErrorCase.PASS_TYPE_NOT_FOUND_ID

object CreatePassResponseMapper {
    fun CreatePassResponse.toDto(): PassDto {
        when (responseCase!!) {
            CreatePassResponse.ResponseCase.SUCCESS -> return success.pass.fromProtoToDto()
            CreatePassResponse.ResponseCase.FAILURE -> failureCase()
            CreatePassResponse.ResponseCase.RESPONSE_NOT_SET -> throw InternalRuntimeException()
        }
    }

    fun PassDto.toCreatePassRequest(): CreatePassRequest {
        return CreatePassRequest.newBuilder().also {
            it.passToCreate = toProto()
        }.build()
    }

    private fun CreatePassResponse.failureCase(): Nothing {
        val message = failure.message.orEmpty()
        when (this.failure.errorCase!!) {
            PASS_TYPE_NOT_FOUND_ID -> throw PassTypeNotFoundException(message)
            OWNER_NOT_FOUND_BY_ID -> throw PassOwnerNotFoundException(message)
            CreatePassResponse.Failure.ErrorCase.ERROR_NOT_SET -> throw InternalRuntimeException(message)
        }
    }
}
