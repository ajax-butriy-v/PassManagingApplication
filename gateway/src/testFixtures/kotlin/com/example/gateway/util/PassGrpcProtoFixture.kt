package com.example.gateway.util

import com.example.commonmodel.Error
import com.example.commonmodel.Pass
import com.example.gateway.mapper.rest.FindPassByIdResponseMapper.toProto
import com.example.gateway.util.PassDtoFixture.passDto
import com.example.grpcapi.reqrep.pass.CancelPassRequest
import com.example.grpcapi.reqrep.pass.CancelPassResponse
import com.example.grpcapi.reqrep.pass.CreatePassResponse
import com.example.grpcapi.reqrep.pass.DeletePassByIdRequest
import com.example.grpcapi.reqrep.pass.DeletePassByIdResponse
import com.example.grpcapi.reqrep.pass.FindPassByIdRequest
import com.example.grpcapi.reqrep.pass.FindPassByIdResponse
import com.example.grpcapi.reqrep.pass.TransferPassRequest
import com.example.grpcapi.reqrep.pass.TransferPassResponse
import org.bson.types.ObjectId

object PassGrpcProtoFixture {
    private const val PASS_OWNER_NOT_FOUND = "Could not find pass owner by id "
    private const val PASS_NOT_FOUND = "Could not find pass by id "
    private const val PASS_TYPE_NOT_FOUND = "Could not find pass type by id "

    val protoPass = passDto.toProto()
    fun cancelPassRequest(passId: String, passOwnerId: String): CancelPassRequest {
        return CancelPassRequest.newBuilder().apply {
            id = passId
            ownerId = passOwnerId
        }.build()
    }

    val succesfulCancelPassResponse = CancelPassResponse.newBuilder().apply {
        successBuilder
    }.build()

    fun failureCancelPassWithPassOwnerNotFoundResponse(ownerId: ObjectId): CancelPassResponse {
        return CancelPassResponse.newBuilder().apply {
            failureBuilder.passOwnerNotFoundById = Error.getDefaultInstance()
            failureBuilder.message = PASS_OWNER_NOT_FOUND + ownerId
        }.build()
    }

    fun failureCancelPassResponse(throwable: Throwable): CancelPassResponse {
        return CancelPassResponse.newBuilder().apply {
            failureBuilder.message = throwable.message.orEmpty()
        }.build()
    }

    fun successfulCreatePassResponse(pass: Pass): CreatePassResponse {
        return CreatePassResponse.newBuilder().apply {
            successBuilder.pass = pass
        }.build()
    }

    fun failureCreatePassResponseWithPassOwnerNotFound(ownerId: String): CreatePassResponse {
        return CreatePassResponse.newBuilder().apply {
            failureBuilder.ownerNotFoundById = Error.getDefaultInstance()
            failureBuilder.message = PASS_OWNER_NOT_FOUND + ownerId
        }.build()
    }

    fun failureCreatePassResponseWithPassTypeNotFound(passTypeId: ObjectId): CreatePassResponse {
        return CreatePassResponse.newBuilder().apply {
            failureBuilder.passTypeNotFoundId = Error.getDefaultInstance()
            failureBuilder.message = PASS_TYPE_NOT_FOUND + passTypeId
        }.build()
    }

    fun failureCreatePassResponse(throwable: Throwable): CreatePassResponse {
        return CreatePassResponse.newBuilder().apply {
            failureBuilder.message = throwable.message.orEmpty()
        }.build()
    }

    fun deletePassByIdRequest(passId: String): DeletePassByIdRequest {
        return DeletePassByIdRequest.newBuilder().apply {
            id = passId
        }.build()
    }

    val succesfulDeletePassByIdResponse = DeletePassByIdResponse.newBuilder().apply {
        successBuilder
    }.build()

    fun failureDeletePassByIdResponse(throwable: Throwable): DeletePassByIdResponse {
        return DeletePassByIdResponse.newBuilder().apply {
            failureBuilder.message = throwable.message.orEmpty()
        }.build()
    }

    fun findPassByIdRequest(passId: String): FindPassByIdRequest {
        return FindPassByIdRequest.newBuilder().apply {
            id = passId
        }.build()
    }

    fun successfulFindPassByIdResponse(pass: Pass): FindPassByIdResponse {
        return FindPassByIdResponse.newBuilder().apply {
            successBuilder.pass = pass
        }.build()
    }

    fun failureFindPassByIdResponseWithPassNotFound(passId: String): FindPassByIdResponse {
        return FindPassByIdResponse.newBuilder().apply {
            failureBuilder.notFoundById = Error.getDefaultInstance()
            failureBuilder.message = PASS_NOT_FOUND + passId
        }.build()
    }

    fun transferPassRequest(passId: String, passOwnerId: String): TransferPassRequest {
        return TransferPassRequest.newBuilder().apply {
            id = passId
            ownerId = passOwnerId
        }.build()
    }

    val successfulTransferPassResponse = TransferPassResponse.newBuilder().apply {
        successBuilder
    }.build()

    fun failureTransferPassResponseWithPassOwnerNotFound(ownerId: ObjectId): TransferPassResponse {
        return TransferPassResponse.newBuilder().apply {
            failureBuilder.passOwnerNotFoundById = Error.getDefaultInstance()
            failureBuilder.message = PASS_OWNER_NOT_FOUND + ownerId
        }.build()
    }

    fun failureTransferPassResponseWithPassNotFound(passId: ObjectId): TransferPassResponse {
        return TransferPassResponse.newBuilder().apply {
            failureBuilder.passNotFoundById = Error.getDefaultInstance()
            failureBuilder.message = PASS_NOT_FOUND + passId
        }.build()
    }

    fun failureTransferPassResponse(throwable: Throwable): TransferPassResponse {
        return TransferPassResponse.newBuilder().apply {
            failureBuilder.message = throwable.message.orEmpty()
        }.build()
    }
}