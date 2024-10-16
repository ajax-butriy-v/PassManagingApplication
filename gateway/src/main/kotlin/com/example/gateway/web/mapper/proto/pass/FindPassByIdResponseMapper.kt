package com.example.gateway.web.mapper.proto.pass

import com.example.gateway.web.dto.PassDto
import com.example.passmanagersvc.exception.PassNotFoundException
import com.example.passmanagersvc.input.reqreply.FindPassByIdResponse
import com.example.passmanagersvc.input.reqreply.FindPassByIdResponse.Failure.ErrorCase.ERROR_NOT_SET
import com.example.passmanagersvc.input.reqreply.FindPassByIdResponse.Failure.ErrorCase.NOT_FOUND_BY_ID
import com.example.passmanagersvc.web.mapper.proto.DecimalProtoMapper.toBigDecimal

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
            return success.pass.run {
                PassDto(
                    purchasedFor = purchasedFor.toBigDecimal(),
                    passOwnerId = passOwnerId,
                    passTypeId = passTypeId
                )
            }
        }
    }
}
