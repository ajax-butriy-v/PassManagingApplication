package com.example.gateway.infrastructure.rest.mapper

import com.example.commonmodels.Pass
import com.example.core.exception.InternalRuntimeException
import com.example.core.exception.PassNotFoundException
import com.example.core.mapper.proto.DecimalProtoMapper.toBDecimal
import com.example.core.mapper.proto.DecimalProtoMapper.toBigDecimal
import com.example.gateway.infrastructure.rest.dto.PassDto
import com.example.internal.input.reqreply.FindPassByIdResponse
import com.example.internal.input.reqreply.FindPassByIdResponse.Failure.ErrorCase.ERROR_NOT_SET
import com.example.internal.input.reqreply.FindPassByIdResponse.Failure.ErrorCase.NOT_FOUND_BY_ID

object FindPassByIdResponseMapper {
    fun FindPassByIdResponse.toDto(): PassDto {
        when (responseCase!!) {
            FindPassByIdResponse.ResponseCase.SUCCESS -> return success.pass.fromProtoToDto()
            FindPassByIdResponse.ResponseCase.FAILURE -> failureCase()
            FindPassByIdResponse.ResponseCase.RESPONSE_NOT_SET -> throw InternalRuntimeException()
        }
    }

    fun Pass.fromProtoToDto(): PassDto {
        return PassDto(
            purchasedFor = purchasedFor.toBigDecimal(),
            passOwnerId = passOwnerId,
            passTypeId = passTypeId
        )
    }

    fun PassDto.toProto(): Pass {
        return Pass.newBuilder()
            .setPassOwnerId(passOwnerId)
            .setPassTypeId(passTypeId)
            .setPurchasedFor(purchasedFor.toBDecimal())
            .build()
    }

    private fun FindPassByIdResponse.failureCase(): Nothing {
        val message = failure.message.orEmpty()
        when (failure.errorCase!!) {
            NOT_FOUND_BY_ID -> throw PassNotFoundException(message)
            ERROR_NOT_SET -> throw InternalRuntimeException(message)
        }
    }
}
