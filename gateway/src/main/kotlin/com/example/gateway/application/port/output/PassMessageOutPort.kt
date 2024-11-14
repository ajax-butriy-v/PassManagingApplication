package com.example.gateway.application.port.output

import com.example.internal.input.reqreply.CreatePassRequest
import com.example.internal.input.reqreply.CreatePassResponse
import com.example.internal.input.reqreply.FindPassByIdRequest
import com.example.internal.input.reqreply.FindPassByIdResponse
import com.example.internal.input.reqreply.TransferredPassMessage
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface PassMessageOutPort {
    fun getAllTransferredPasses(passTypeName: String): Flux<TransferredPassMessage>
    fun findPassById(request: FindPassByIdRequest): Mono<FindPassByIdResponse>
    fun createPass(request: CreatePassRequest): Mono<CreatePassResponse>
}