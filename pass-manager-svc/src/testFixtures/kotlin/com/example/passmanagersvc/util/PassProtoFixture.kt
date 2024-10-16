package com.example.passmanagersvc.util

import com.example.passmanagersvc.commonmodels.Error
import com.example.passmanagersvc.domain.MongoPass
import com.example.passmanagersvc.input.reqreply.CancelPassRequest
import com.example.passmanagersvc.input.reqreply.CancelPassResponse
import com.example.passmanagersvc.input.reqreply.CreatePassRequest
import com.example.passmanagersvc.input.reqreply.CreatePassResponse
import com.example.passmanagersvc.input.reqreply.DeletePassByIdRequest
import com.example.passmanagersvc.input.reqreply.DeletePassByIdResponse
import com.example.passmanagersvc.input.reqreply.FindPassByIdRequest
import com.example.passmanagersvc.input.reqreply.FindPassByIdResponse
import com.example.passmanagersvc.input.reqreply.TransferPassRequest
import com.example.passmanagersvc.input.reqreply.TransferPassResponse
import com.example.passmanagersvc.util.PassFixture.passToCreate
import com.example.passmanagersvc.web.mapper.proto.DecimalProtoMapper.toBDecimal
import com.example.passmanagersvc.web.mapper.proto.pass.FindPassByIdMapper.toProto
import org.bson.types.ObjectId

object PassProtoFixture {
    private const val PASS_OWNER_NOT_FOUND = "Could not find pass owner by id "
    private const val PASS_NOT_FOUND = "Could not find pass by id "
    private const val PASS_TYPE_NOT_FOUND = "Could not find pass type by id "

    fun cancelPassRequest(passId: ObjectId?, passOwnerId: ObjectId?): CancelPassRequest {
        return CancelPassRequest.newBuilder()
            .setId(passId.toString())
            .setOwnerId(passOwnerId.toString())
            .build()
    }

    val succesfulCancelPassResponse = CancelPassResponse.newBuilder().apply {
        successBuilder
    }.build()

    fun failureCancelPassResponseWithOwnerNotFound(ownerId: ObjectId?): CancelPassResponse {
        return CancelPassResponse.newBuilder().apply {
            failureBuilder.setPassOwnerNotFoundById(Error.getDefaultInstance())
            failureBuilder.setMessage(PASS_OWNER_NOT_FOUND + ownerId)
        }.build()
    }

    fun createPassRequest(passOwnerId: String, passTypeId: String): CreatePassRequest {
        return CreatePassRequest.newBuilder()
            .setPassOwnerId(passOwnerId)
            .setPassTypeId(passTypeId)
            .setPurchasedFor(passToCreate.purchasedFor!!.toBDecimal())
            .build()
    }

    fun successfulCreatePassResponse(pass: MongoPass): CreatePassResponse {
        return CreatePassResponse.newBuilder().apply {
            successBuilder.setPass(pass.toProto())
        }.build()
    }

    fun failureCreatePassResponseWithPassOwnerNotFound(ownerId: String): CreatePassResponse {
        return CreatePassResponse.newBuilder().apply {
            failureBuilder.setOwnerNotFoundById(Error.getDefaultInstance())
            failureBuilder.setMessage(PASS_OWNER_NOT_FOUND + ownerId)
        }.build()
    }

    fun failureCreatePassResponseWithPassTypeNotFound(passTypeId: ObjectId?): CreatePassResponse {
        return CreatePassResponse.newBuilder().apply {
            failureBuilder.setPassTypeNotFoundId(Error.getDefaultInstance())
            failureBuilder.setMessage(PASS_TYPE_NOT_FOUND + passTypeId)
        }.build()
    }

    fun deletePassByIdRequest(passId: ObjectId?): DeletePassByIdRequest {
        return DeletePassByIdRequest.newBuilder()
            .setId(passId.toString())
            .build()
    }

    val succesfulDeletePassByIdResponse = DeletePassByIdResponse.newBuilder().apply {
        successBuilder
    }.build()


    fun findPassByIdRequest(passId: ObjectId?): FindPassByIdRequest {
        return FindPassByIdRequest.newBuilder()
            .setId(passId.toString())
            .build()
    }

    fun successfulFindPassByIdResponse(pass: MongoPass): FindPassByIdResponse {
        return FindPassByIdResponse.newBuilder().apply {
            successBuilder.setPass(pass.toProto())
        }.build()
    }

    fun failureFindPassByIdResponseWithPassNotFound(passId: ObjectId?): FindPassByIdResponse {
        return FindPassByIdResponse.newBuilder().apply {
            failureBuilder.setNotFoundById(Error.getDefaultInstance())
            failureBuilder.setMessage(PASS_NOT_FOUND + passId)
        }.build()
    }

    fun transferPassRequest(passId: ObjectId?, passOwnerId: ObjectId?): TransferPassRequest {
        return TransferPassRequest.newBuilder()
            .setId(passId.toString())
            .setOwnerId(passOwnerId.toString())
            .build()
    }

    val successfulTransferPassResponse = TransferPassResponse.newBuilder().apply {
        successBuilder
    }.build()

    fun failureTransferPassResponseWithPassOwnerNotFound(ownerId: ObjectId?): TransferPassResponse {
        return TransferPassResponse.newBuilder().apply {
            failureBuilder.setPassOwnerNotFoundById(Error.getDefaultInstance())
            failureBuilder.setMessage(PASS_OWNER_NOT_FOUND + ownerId)
        }.build()
    }

    fun failureTransferPassResponseWithPassNotFound(passId: ObjectId?): TransferPassResponse {
        return TransferPassResponse.newBuilder().apply {
            failureBuilder.setPassNotFoundById(Error.getDefaultInstance())
            failureBuilder.setMessage(PASS_NOT_FOUND + passId)
        }.build()
    }

}