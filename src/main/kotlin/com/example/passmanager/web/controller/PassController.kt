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
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/passes")
internal class PassController(
    private val passService: PassService,
    private val passManagementService: PassManagementService,
) {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: String): ResponseEntity<PassDto> {
        val passById = passService.findById(id)
        return passById?.run { ResponseEntity.ok(toDto()) } ?: ResponseEntity.notFound().build()
    }

    @PostMapping
    fun create(@Valid @RequestBody passDto: PassDto): ResponseEntity<PassDto> {
        val created = passService.create(passDto.toEntity(), passDto.passOwnerId, passDto.passTypeId)
        return ResponseEntity.status(HttpStatus.CREATED).body(created.toDto())
    }

    @PostMapping("/{id}/cancel/{owner-id}")
    fun cancelPass(@PathVariable id: String, @PathVariable("owner-id") ownerId: String): ResponseEntity<Boolean> {
        return passManagementService.cancelPass(ownerId, id).let { ResponseEntity.ok(it) }
    }

    @PostMapping("/{id}/transfer/{owner-id}")
    fun transferPass(@PathVariable id: String, @PathVariable("owner-id") ownerId: String): ResponseEntity<Unit> {
        passManagementService.transferPass(id, ownerId)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/{id}")
    fun deleteById(@PathVariable id: String): ResponseEntity<Unit> {
        passService.deleteById(id)
        return ResponseEntity.noContent().build()
    }
}
