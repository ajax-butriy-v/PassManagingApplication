package com.example.gateway.web.mapper.proto.pass

import com.example.gateway.web.dto.PassDto
import com.example.passmanagersvc.exception.PassOwnerNotFoundException
import com.example.passmanagersvc.exception.PassTypeNotFoundException
import com.example.passmanagersvc.input.reqreply.CreatePassRequest
import com.example.passmanagersvc.input.reqreply.CreatePassResponse
import com.example.passmanagersvc.input.reqreply.CreatePassResponse.Failure.ErrorCase.OWNER_NOT_FOUND_BY_ID
import com.example.passmanagersvc.input.reqreply.CreatePassResponse.Failure.ErrorCase.PASS_TYPE_NOT_FOUND_ID
import com.example.passmanagersvc.web.mapper.proto.DecimalProtoMapper.toBDecimal
import com.example.passmanagersvc.web.mapper.proto.DecimalProtoMapper.toBigDecimal

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
            return success.pass.run {
                PassDto(
                    purchasedFor = purchasedFor.toBigDecimal(),
                    passOwnerId = passOwnerId,
                    passTypeId = passTypeId
                )
            }
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
