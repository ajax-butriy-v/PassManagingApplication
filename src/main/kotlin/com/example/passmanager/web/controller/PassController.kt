package com.example.passmanager.web.controller

import com.example.passmanager.service.PassManagementService
import com.example.passmanager.service.PassService
import com.example.passmanager.web.dto.PassDto
import com.example.passmanager.web.mapper.PassMapper.toDto
import com.example.passmanager.web.mapper.PassMapper.toEntity
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
internal class PassController(
    private val passService: PassService,
    private val passManagementService: PassManagementService,
) {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: String): Mono<ResponseEntity<PassDto>> {
        return passService.findById(id)
            .map { it.toDto() }
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody passDto: PassDto): Mono<PassDto> {
        return passService.create(passDto.toEntity(), passDto.passOwnerId, passDto.passTypeId).map { it.toDto() }
    }

    @PostMapping("/{id}/cancel/{owner-id}")
    @ResponseStatus(HttpStatus.OK)
    fun cancelPass(@PathVariable id: String, @PathVariable("owner-id") ownerId: String): Mono<Unit> {
        return passManagementService.cancelPass(ownerId, id)
    }

    @PostMapping("/{id}/transfer/{owner-id}")
    @ResponseStatus(HttpStatus.OK)
    fun transferPass(@PathVariable id: String, @PathVariable("owner-id") ownerId: String): Mono<Unit> {
        return passManagementService.transferPass(id, ownerId)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteById(@PathVariable id: String): Mono<Unit> {
        return passService.deleteById(id)
    }
}
