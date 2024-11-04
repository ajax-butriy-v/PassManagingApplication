package com.example.gateway.grpc

import com.example.commonmodels.Pass
import com.example.gateway.configuration.NatsClient
import com.example.gateway.mapper.grpc.CreatePassMapper.toGrpcProto
import com.example.gateway.mapper.grpc.CreatePassMapper.toInternalProto
import com.example.gateway.mapper.grpc.FindPassByIdMapper.toGrpcProto
import com.example.gateway.mapper.grpc.FindPassByIdMapper.toInternalProto
import com.example.grpcapi.reqrep.pass.CreatePassRequest
import com.example.grpcapi.reqrep.pass.CreatePassResponse
import com.example.grpcapi.reqrep.pass.FindPassByIdRequest
import com.example.grpcapi.reqrep.pass.FindPassByIdResponse
import com.example.grpcapi.reqrep.pass.GetAllTransferredPassesRequest
import com.example.grpcapi.service.ReactorPassServiceGrpc
import com.example.internal.NatsSubject.Pass.CREATE
import com.example.internal.NatsSubject.Pass.FIND_BY_ID
import net.devh.boot.grpc.server.service.GrpcService
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import com.example.internal.input.reqreply.CreatePassResponse as InternalCreatePassResponse
import com.example.internal.input.reqreply.FindPassByIdResponse as InternalFindPassByIdResponse

@GrpcService
class GrpcPassService(
    private val natsClient: NatsClient,
) : ReactorPassServiceGrpc.PassServiceImplBase() {
    override fun getAllTransferredPasses(request: Mono<GetAllTransferredPassesRequest>): Flux<Pass> {
        return request.map { it.passTypeName }
            .flatMapMany { passTypeName -> natsClient.subscribeToPassesByType(passTypeName) }
    }

    override fun findPassById(request: Mono<FindPassByIdRequest>): Mono<FindPassByIdResponse> {
        return request.map { it.toInternalProto() }
            .flatMap { natsClient.request(FIND_BY_ID, it, InternalFindPassByIdResponse.parser()) }
            .map { it.toGrpcProto() }
    }

    override fun createPass(request: Mono<CreatePassRequest>): Mono<CreatePassResponse> {
        return request.map { it.toInternalProto() }
            .flatMap { natsClient.request(CREATE, it, InternalCreatePassResponse.parser()) }
            .map { it.toGrpcProto() }
    }
}
