package com.example.gateway.infrastructure.grpc

import com.example.commonmodels.Pass
import com.example.gateway.application.port.output.PassMessageOutPort
import com.example.gateway.infrastructure.mapper.grpc.CreatePassMapper.toGrpcProto
import com.example.gateway.infrastructure.mapper.grpc.CreatePassMapper.toInternalProto
import com.example.gateway.infrastructure.mapper.grpc.FindPassByIdMapper.toGrpcProto
import com.example.gateway.infrastructure.mapper.grpc.FindPassByIdMapper.toInternalProto
import com.example.grpcapi.reqrep.pass.CreatePassRequest
import com.example.grpcapi.reqrep.pass.CreatePassResponse
import com.example.grpcapi.reqrep.pass.FindPassByIdRequest
import com.example.grpcapi.reqrep.pass.FindPassByIdResponse
import com.example.grpcapi.reqrep.pass.GetAllTransferredPassesRequest
import com.example.grpcapi.service.ReactorPassServiceGrpc
import com.example.internal.input.reqreply.TransferredPassMessage
import net.devh.boot.grpc.server.service.GrpcService
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@GrpcService
class GrpcPassService(
    private val passMessageOutPort: PassMessageOutPort,
) : ReactorPassServiceGrpc.PassServiceImplBase() {

    override fun getAllTransferredPasses(request: Mono<GetAllTransferredPassesRequest>): Flux<Pass> {
        return request.map { it.passTypeName }
            .flatMapMany { passMessageOutPort.getAllTransferredPasses(it) }
            .map(TransferredPassMessage::getPass)
    }

    override fun findPassById(request: Mono<FindPassByIdRequest>): Mono<FindPassByIdResponse> {
        return request.map { it.toInternalProto() }
            .flatMap { passMessageOutPort.findPassById(it) }
            .map { it.toGrpcProto() }
    }

    override fun createPass(request: Mono<CreatePassRequest>): Mono<CreatePassResponse> {
        return request.map { it.toInternalProto() }
            .flatMap { passMessageOutPort.createPass(it) }
            .map { it.toGrpcProto() }
    }
}
