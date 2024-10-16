package com.example.gateway.web.rest

import com.example.gateway.configuration.NatsClient
import com.example.gateway.web.dto.PassDto
import com.example.gateway.web.mapper.proto.pass.CancelPassResponseMapper.toUnitResponse
import com.example.gateway.web.mapper.proto.pass.CreatePassResponseMapper.toCreatePassRequest
import com.example.gateway.web.mapper.proto.pass.CreatePassResponseMapper.toDto
import com.example.gateway.web.mapper.proto.pass.DeletePassByIdResponseMapper.toDeleteResponse
import com.example.gateway.web.mapper.proto.pass.FindPassByIdResponseMapper.toDto
import com.example.gateway.web.mapper.proto.pass.TransferPassResponseMapper.toTransferResponse
import com.example.internal.NatsSubject.Pass.CANCEL
import com.example.internal.NatsSubject.Pass.CREATE
import com.example.internal.NatsSubject.Pass.DELETE_BY_ID
import com.example.internal.NatsSubject.Pass.FIND_BY_ID
import com.example.internal.NatsSubject.Pass.TRANSFER
import com.example.passmanagersvc.input.reqreply.CancelPassRequest
import com.example.passmanagersvc.input.reqreply.CancelPassResponse
import com.example.passmanagersvc.input.reqreply.CreatePassResponse
import com.example.passmanagersvc.input.reqreply.DeletePassByIdRequest
import com.example.passmanagersvc.input.reqreply.DeletePassByIdResponse
import com.example.passmanagersvc.input.reqreply.FindPassByIdRequest
import com.example.passmanagersvc.input.reqreply.FindPassByIdResponse
import com.example.passmanagersvc.input.reqreply.TransferPassRequest
import com.example.passmanagersvc.input.reqreply.TransferPassResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/passes")
internal class PassController(private val natsClient: NatsClient) {

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun findById(@PathVariable id: String): Mono<PassDto> {
        val payload = FindPassByIdRequest.newBuilder()
            .setId(id)
            .build()
        return payload.let { natsClient.request(FIND_BY_ID, it, FindPassByIdResponse.parser()) }.map { it.toDto() }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody passDto: PassDto): Mono<PassDto> {
        val payload = passDto.toCreatePassRequest()
        return natsClient.request(CREATE, payload, CreatePassResponse.parser()).map { it.toDto() }
    }

    @PostMapping("/{id}/cancel/{owner-id}")
    @ResponseStatus(HttpStatus.OK)
    fun cancelPass(@PathVariable id: String, @PathVariable("owner-id") ownerId: String): Mono<Unit> {
        val payload = CancelPassRequest.newBuilder()
            .setId(id)
            .setOwnerId(ownerId)
            .build()
        return payload.let { natsClient.request(CANCEL, it, CancelPassResponse.parser()) }.map { it.toUnitResponse() }
    }

    @PostMapping("/{id}/transfer/{owner-id}")
    @ResponseStatus(HttpStatus.OK)
    fun transferPass(@PathVariable id: String, @PathVariable("owner-id") ownerId: String): Mono<Unit> {
        val payload = TransferPassRequest.newBuilder()
            .setId(id)
            .setOwnerId(ownerId)
            .build()
        return payload.let { natsClient.request(TRANSFER, it, TransferPassResponse.parser()) }
            .map { it.toTransferResponse() }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteById(@PathVariable id: String): Mono<Unit> {
        val payload = DeletePassByIdRequest.newBuilder()
            .setId(id)
            .build()
        return payload.let { natsClient.request(DELETE_BY_ID, it, DeletePassByIdResponse.parser()) }
            .map { it.toDeleteResponse() }
    }
}
