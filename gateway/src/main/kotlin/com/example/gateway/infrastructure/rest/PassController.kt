package com.example.gateway.infrastructure.rest

import com.example.gateway.infrastructure.rest.dto.PassDto
import com.example.gateway.infrastructure.rest.mapper.CancelPassResponseMapper.toUnitResponse
import com.example.gateway.infrastructure.rest.mapper.CreatePassResponseMapper.toCreatePassRequest
import com.example.gateway.infrastructure.rest.mapper.CreatePassResponseMapper.toDto
import com.example.gateway.infrastructure.rest.mapper.DeletePassByIdResponseMapper.toDeleteResponse
import com.example.gateway.infrastructure.rest.mapper.FindPassByIdResponseMapper.toDto
import com.example.gateway.infrastructure.rest.mapper.TransferPassResponseMapper.toUnitResponse
import com.example.internal.NatsSubject.Pass.CANCEL
import com.example.internal.NatsSubject.Pass.CREATE
import com.example.internal.NatsSubject.Pass.DELETE_BY_ID
import com.example.internal.NatsSubject.Pass.FIND_BY_ID
import com.example.internal.NatsSubject.Pass.TRANSFER
import com.example.internal.input.reqreply.CancelPassRequest
import com.example.internal.input.reqreply.CancelPassResponse
import com.example.internal.input.reqreply.CreatePassResponse
import com.example.internal.input.reqreply.DeletePassByIdRequest
import com.example.internal.input.reqreply.DeletePassByIdResponse
import com.example.internal.input.reqreply.FindPassByIdRequest
import com.example.internal.input.reqreply.FindPassByIdResponse
import com.example.internal.input.reqreply.TransferPassRequest
import com.example.internal.input.reqreply.TransferPassResponse
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
import systems.ajax.nats.publisher.api.NatsMessagePublisher

@RestController
@RequestMapping("/passes")
internal class PassController(private val natsMessagePublisher: NatsMessagePublisher) {

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun findById(@PathVariable id: String): Mono<PassDto> {
        val payload = FindPassByIdRequest.newBuilder()
            .setId(id)
            .build()
        return natsMessagePublisher.request(FIND_BY_ID, payload, FindPassByIdResponse.parser()).map { it.toDto() }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody passDto: PassDto): Mono<PassDto> {
        val payload = passDto.toCreatePassRequest()
        return natsMessagePublisher.request(CREATE, payload, CreatePassResponse.parser()).map { it.toDto() }
    }

    @PostMapping("/{id}/cancel/{owner-id}")
    @ResponseStatus(HttpStatus.OK)
    fun cancelPass(@PathVariable id: String, @PathVariable("owner-id") ownerId: String): Mono<Unit> {
        val payload = CancelPassRequest.newBuilder()
            .setId(id)
            .setOwnerId(ownerId)
            .build()
        return natsMessagePublisher.request(CANCEL, payload, CancelPassResponse.parser()).map { it.toUnitResponse() }
    }

    @PostMapping("/{id}/transfer/{owner-id}")
    @ResponseStatus(HttpStatus.OK)
    fun transferPass(@PathVariable id: String, @PathVariable("owner-id") ownerId: String): Mono<Unit> {
        val payload = TransferPassRequest.newBuilder()
            .setId(id)
            .setOwnerId(ownerId)
            .build()
        return natsMessagePublisher.request(TRANSFER, payload, TransferPassResponse.parser())
            .map { it.toUnitResponse() }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteById(@PathVariable id: String): Mono<Unit> {
        val payload = DeletePassByIdRequest.newBuilder()
            .setId(id)
            .build()
        return natsMessagePublisher.request(DELETE_BY_ID, payload, DeletePassByIdResponse.parser())
            .map { it.toDeleteResponse() }
    }
}
