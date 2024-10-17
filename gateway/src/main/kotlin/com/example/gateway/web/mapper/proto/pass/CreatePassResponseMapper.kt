package com.example.gateway.web.mapper.proto.pass

import com.example.core.exception.PassOwnerNotFoundException
import com.example.core.exception.PassTypeNotFoundException
import com.example.core.web.mapper.proto.DecimalProtoMapper.toBDecimal
import com.example.gateway.web.dto.PassDto
import com.example.gateway.web.mapper.proto.pass.FindPassByIdResponseMapper.fromProtoToDto
import com.example.internal.input.reqreply.CreatePassRequest
import com.example.internal.input.reqreply.CreatePassResponse
import com.example.internal.input.reqreply.CreatePassResponse.Failure.ErrorCase.OWNER_NOT_FOUND_BY_ID
import com.example.internal.input.reqreply.CreatePassResponse.Failure.ErrorCase.PASS_TYPE_NOT_FOUND_ID

object CreatePassResponseMapper {
    fun CreatePassResponse.toDto(): PassDto {
        require(this != CreatePassResponse.getDefaultInstance()) {
            "Response must not be default instance."
        }

        if (hasFailure()) {
            val message = failure.message.orEmpty()
            when (this.failure.errorCase!!) {
                PASS_TYPE_NOT_FOUND_ID -> throw PassTypeNotFoundException(message)
                OWNER_NOT_FOUND_BY_ID -> throw PassOwnerNotFoundException(message)
                CreatePassResponse.Failure.ErrorCase.ERROR_NOT_SET -> error(message)
            }
        } else {
            return success.pass.fromProtoToDto()
        }
    }

    fun PassDto.toCreatePassRequest(): CreatePassRequest {
        return CreatePassRequest.newBuilder()
            .setPassTypeId(passTypeId)
            .setPassOwnerId(passOwnerId)
            .setPurchasedFor(purchasedFor.toBDecimal())
            .build()
    }
}
