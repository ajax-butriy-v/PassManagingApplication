package com.example.gateway.web.mapper.proto.pass

import com.example.core.exception.PassNotFoundException
import com.example.core.web.mapper.proto.DecimalProtoMapper.toBDecimal
import com.example.core.web.mapper.proto.DecimalProtoMapper.toBigDecimal
import com.example.gateway.web.dto.PassDto
import com.example.internal.commonmodels.Pass
import com.example.internal.input.reqreply.FindPassByIdResponse
import com.example.internal.input.reqreply.FindPassByIdResponse.Failure.ErrorCase.ERROR_NOT_SET
import com.example.internal.input.reqreply.FindPassByIdResponse.Failure.ErrorCase.NOT_FOUND_BY_ID

object FindPassByIdResponseMapper {
    fun FindPassByIdResponse.toDto(): PassDto {
        require(this != FindPassByIdResponse.getDefaultInstance()) {
            "Response must not be default instance."
        }
        if (hasFailure()) {
            val message = failure.message.orEmpty()
            when (failure.errorCase!!) {
                NOT_FOUND_BY_ID -> throw PassNotFoundException(message)
                ERROR_NOT_SET -> error(message)
            }
        } else {
            return success.pass.fromProtoToDto()
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
}
