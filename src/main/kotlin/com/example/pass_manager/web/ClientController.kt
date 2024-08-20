package com.example.pass_manager.web

import com.example.pass_manager.service.ClientService
import com.example.pass_manager.web.dto.ClientDto
import com.example.pass_manager.web.dto.SpentAfterDateDto
import com.example.pass_manager.web.mapper.MongoClientMapper.partialUpdate
import com.example.pass_manager.web.mapper.MongoClientMapper.toDto
import com.example.pass_manager.web.mapper.MongoClientMapper.toEntity
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/clients")
class ClientController(private val clientService: ClientService) {

    @GetMapping("/{id}")
    fun calculateSpentAfterDate(
        @RequestParam afterDate: Instant,
        @PathVariable("id") clientId: String,
    ): ResponseEntity<SpentAfterDateDto> {
        val totalSpent = clientService.calculateSpentAfterDate(afterDate, clientId.toObjectId())
        return ResponseEntity.ok(SpentAfterDateDto(clientId, afterDate, totalSpent))
    }

    @PostMapping
    fun create(@Valid @RequestBody dto: ClientDto): ResponseEntity<ClientDto> {
        val created = clientService.create(dto.toEntity())
        return ResponseEntity.status(HttpStatus.CREATED).body(created.toDto())
    }

    @PostMapping("/{id}/cancel/{pass-id}")
    fun cancelPass(
        @PathVariable("id") clientId: String,
        @PathVariable("pass-id") passId: String,
    ): ResponseEntity<Unit> {
        val isCanceled = clientService.cancelPass(clientId.toObjectId(), passId.toObjectId())
        return if (isCanceled) ResponseEntity.ok().build() else ResponseEntity.badRequest().build()
    }

    @PatchMapping("/{id}")
    fun partialUpdate(
        @Valid @RequestBody dto: ClientDto,
        @PathVariable("id") clientId: String,
    ): ResponseEntity<ClientDto> {
        return clientService.findById(clientId.toObjectId())?.let {
            val mappedClient = it.partialUpdate(dto)
            val updatedInDb = clientService.update(clientId.toObjectId(), mappedClient)
            ResponseEntity.ok(updatedInDb.toDto())
        } ?: ResponseEntity.notFound().build()
    }

    @DeleteMapping("/{id}")
    fun deleteById(@PathVariable id: String): ResponseEntity<Unit> {
        clientService.deleteById(id.toObjectId())
        return ResponseEntity.noContent().build()
    }

}
