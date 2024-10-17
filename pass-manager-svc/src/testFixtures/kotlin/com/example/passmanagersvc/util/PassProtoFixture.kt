package com.example.passmanagersvc.util

import com.example.internal.commonmodels.Error
import com.example.internal.input.reqreply.CancelPassRequest
import com.example.internal.input.reqreply.CancelPassResponse
import com.example.internal.input.reqreply.CreatePassRequest
import com.example.internal.input.reqreply.CreatePassResponse
import com.example.internal.input.reqreply.DeletePassByIdRequest
import com.example.internal.input.reqreply.DeletePassByIdResponse
import com.example.internal.input.reqreply.FindPassByIdRequest
import com.example.internal.input.reqreply.FindPassByIdResponse
import com.example.internal.input.reqreply.TransferPassRequest
import com.example.internal.input.reqreply.TransferPassResponse
import com.example.passmanagersvc.util.PassFixture.passToCreate
import com.example.passmanagersvc.web.mapper.proto.DecimalProtoMapper.toBDecimal
import org.bson.types.ObjectId

object PassProtoFixture {
    private const val PASS_OWNER_NOT_FOUND = "Could not find pass owner by id "
    private const val PASS_NOT_FOUND = "Could not find pass by id "
    private const val PASS_TYPE_NOT_FOUND = "Could not find pass type by id "

    fun cancelPassRequest(passId: String, passOwnerId: String): CancelPassRequest {
        return CancelPassRequest.newBuilder()
            .setId(passId)
            .setOwnerId(passOwnerId)
            .build()
    }

    val succesfulCancelPassResponse = CancelPassResponse.newBuilder().apply {
        successBuilder
    }.build()

    fun failureCancelPassResponseWithOwnerNotFound(ownerId: String): CancelPassResponse {
        return CancelPassResponse.newBuilder().apply {
            failureBuilder.passOwnerNotFoundById = Error.getDefaultInstance()
            failureBuilder.message = PASS_OWNER_NOT_FOUND + ownerId
        }.build()
    }

    fun createPassRequest(passOwnerId: String, passTypeId: String): CreatePassRequest {
        return CreatePassRequest.newBuilder()
            .setPassOwnerId(passOwnerId)
            .setPassTypeId(passTypeId)
            .setPurchasedFor(passToCreate.purchasedFor!!.toBDecimal())
            .build()
    }

    fun failureCreatePassResponseWithPassOwnerNotFound(ownerId: String): CreatePassResponse {
        return CreatePassResponse.newBuilder().apply {
            failureBuilder.setOwnerNotFoundById(Error.getDefaultInstance())
            failureBuilder.setMessage(PASS_OWNER_NOT_FOUND + ownerId)
        }.build()
    }

    fun failureCreatePassResponseWithPassTypeNotFound(passTypeId: ObjectId): CreatePassResponse {
        return CreatePassResponse.newBuilder().apply {
            failureBuilder.setPassTypeNotFoundId(Error.getDefaultInstance())
            failureBuilder.setMessage(PASS_TYPE_NOT_FOUND + passTypeId)
        }.build()
    }

    fun deletePassByIdRequest(passId: String): DeletePassByIdRequest {
        return DeletePassByIdRequest.newBuilder()
            .setId(passId)
            .build()
    }

    val succesfulDeletePassByIdResponse = DeletePassByIdResponse.newBuilder().apply {
        successBuilder
    }.build()


    fun findPassByIdRequest(passId: String): FindPassByIdRequest {
        return FindPassByIdRequest.newBuilder()
            .setId(passId)
            .build()
    }

    fun failureFindPassByIdResponseWithPassNotFound(passId: String): FindPassByIdResponse {
        return FindPassByIdResponse.newBuilder().apply {
            failureBuilder.setNotFoundById(Error.getDefaultInstance())
            failureBuilder.setMessage(PASS_NOT_FOUND + passId)
        }.build()
    }

    fun transferPassRequest(passId: String, passOwnerId: String): TransferPassRequest {
        return TransferPassRequest.newBuilder()
            .setId(passId)
            .setOwnerId(passOwnerId)
            .build()
    }

    val successfulTransferPassResponse = TransferPassResponse.newBuilder().apply {
        successBuilder
    }.build()

    fun failureTransferPassResponseWithPassOwnerNotFound(ownerId: String): TransferPassResponse {
        return TransferPassResponse.newBuilder().apply {
            failureBuilder.setPassOwnerNotFoundById(Error.getDefaultInstance())
            failureBuilder.setMessage(PASS_OWNER_NOT_FOUND + ownerId)
        }.build()
    }

    fun failureTransferPassResponseWithPassNotFound(passId: String): TransferPassResponse {
        return TransferPassResponse.newBuilder().apply {
            failureBuilder.setPassNotFoundById(Error.getDefaultInstance())
            failureBuilder.setMessage(PASS_NOT_FOUND + passId)
        }.build()
    }
}
