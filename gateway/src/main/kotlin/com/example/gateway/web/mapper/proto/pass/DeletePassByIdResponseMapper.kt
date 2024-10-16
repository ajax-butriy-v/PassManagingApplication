package com.example.gateway.web.mapper.proto.pass

import com.example.passmanagersvc.input.reqreply.DeletePassByIdResponse

object DeletePassByIdResponseMapper {
    fun DeletePassByIdResponse.toDeleteResponse() {
        require(this != DeletePassByIdResponse.getDefaultInstance()) {
            "Response must not be default instance."
        }

        if (hasFailure()) {
            error(failure.message.orEmpty())
        }
    }
}
