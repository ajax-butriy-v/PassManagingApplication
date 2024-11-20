package com.example.gateway.util

import com.example.commonmodels.Pass
import com.example.gateway.infrastructure.rest.mapper.FindPassByIdResponseMapper.toProto
import com.example.gateway.util.PassDtoFixture.passDto
import com.example.grpcapi.reqrep.pass.CreatePassResponse
import com.example.grpcapi.reqrep.pass.FindPassByIdResponse
import com.example.internal.input.reqreply.CreatePassResponse as InternalCreatePassResponse
import com.example.internal.input.reqreply.FindPassByIdResponse as InternalFindPassByIdResponse

object PassGrpcProtoFixture {
    val protoPass = passDto.toProto()

    fun successfulCreatePassResponse(pass: Pass): CreatePassResponse {
        return CreatePassResponse.newBuilder().apply {
            successBuilder.pass = pass
        }.build()
    }


    fun successfulFindPassByIdResponse(pass: Pass): FindPassByIdResponse {
        return FindPassByIdResponse.newBuilder().apply {
            successBuilder.pass = pass
        }.build()
    }

    fun failureFindPassByIdResponseWithPassNotFound(exceptionMessage: String): InternalFindPassByIdResponse {
        return InternalFindPassByIdResponse.newBuilder().apply {
            failureBuilder.notFoundByIdBuilder
            failureBuilder.message = exceptionMessage
        }.build()
    }

    fun failureCreatePassResponseWithPassOwnerNotFound(exceptionMessage: String): InternalCreatePassResponse {
        return InternalCreatePassResponse.newBuilder().apply {
            failureBuilder.ownerNotFoundByIdBuilder
            failureBuilder.message = exceptionMessage
        }.build()
    }

    fun failureCreatePassResponseWithPassTypeNotFound(exceptionMessage: String): InternalCreatePassResponse {
        return InternalCreatePassResponse.newBuilder().apply {
            failureBuilder.passTypeNotFoundIdBuilder
            failureBuilder.message = exceptionMessage
        }.build()
    }
}
