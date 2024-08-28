package com.example.passmanager.web.controller

import com.example.passmanager.service.PassManagementService
import com.example.passmanager.service.PassService
import com.example.passmanager.web.dto.PassDto
import com.example.passmanager.web.mapper.PassMapper.toDto
import com.example.passmanager.web.mapper.PassMapper.toEntity
import com.example.passmanager.web.toObjectId
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/passes")
class PassController(
    private val passService: PassService,
    private val passManagementService: PassManagementService,
) {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: String): ResponseEntity<PassDto> {
        val passById = passService.findById(id.toObjectId())
        return passById?.run { ResponseEntity.ok(toDto()) } ?: ResponseEntity.notFound().build()
    }

    @PostMapping
    fun create(@Valid @RequestBody passDto: PassDto): ResponseEntity<PassDto> {
        val passOwnerId = passDto.passOwnerId.toObjectId()
        val passTypeId = passDto.passTypeId.toObjectId()
        val created = passService.create(passDto.toEntity(), passOwnerId, passTypeId)
        return ResponseEntity.status(HttpStatus.CREATED).body(created.toDto())
    }

    @PostMapping("/{id}/cancel/{owner-id}")
    fun cancelPass(@PathVariable id: String, @PathVariable("owner-id") ownerId: String): ResponseEntity<Boolean> {
        return passManagementService.cancelPass(ownerId.toObjectId(), id.toObjectId()).let { ResponseEntity.ok(it) }
    }

    @PostMapping("/{id}/transfer/{owner-id}")
    fun transferPass(@PathVariable id: String, @PathVariable("owner-id") ownerId: String): ResponseEntity<Unit> {
        passManagementService.transferPass(id.toObjectId(), ownerId.toObjectId())
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/{id}")
    fun deleteById(@PathVariable id: String): ResponseEntity<Unit> {
        passService.deleteById(id.toObjectId())
        return ResponseEntity.noContent().build()
    }

}

