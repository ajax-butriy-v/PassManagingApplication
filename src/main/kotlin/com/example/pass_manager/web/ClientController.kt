package com.example.pass_manager.web

import com.example.pass_manager.service.ClientService
import com.example.pass_manager.web.dto.MongoClientDto
import com.example.pass_manager.web.dto.SpentAfterDateDto
import com.example.pass_manager.web.mapper.MongoClientMapper.partialUpdate
import com.example.pass_manager.web.mapper.MongoClientMapper.toDto
import com.example.pass_manager.web.mapper.MongoClientMapper.toEntity
import jakarta.validation.Valid
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/clients")
class ClientController(private val clientService: ClientService,) {

    @GetMapping("/{id}")
    fun calculateSpentAfterDate(
        @RequestParam afterDate: Instant,
        @PathVariable("id") clientId: ObjectId
    ): ResponseEntity<SpentAfterDateDto> {
        val totalSpent = clientService.calculateSpentAfterDate(afterDate, clientId)
        return ResponseEntity.ok(SpentAfterDateDto(afterDate, clientId, totalSpent))
    }

    @PostMapping
    fun create(@Valid @RequestBody dto: MongoClientDto): ResponseEntity<MongoClientDto> {
        val created = clientService.create(dto.toEntity())
        return ResponseEntity.status(HttpStatus.CREATED).body(created.toDto())
    }

    @PostMapping("/{id}/cancel/{pass-id}")
    fun cancelPass(
        @PathVariable("id") clientId: ObjectId,
        @PathVariable("pass-id") passId: ObjectId
    ): ResponseEntity<Unit> {
        val isCanceled = clientService.cancelPass(clientId, passId)
        return if (isCanceled) ResponseEntity.ok().build() else ResponseEntity.badRequest().build()
    }

    @PatchMapping("/{id}")
    fun partialUpdate(
        @Valid @RequestBody dto: MongoClientDto,
        @PathVariable("id") clientId: ObjectId
    ): ResponseEntity<MongoClientDto> {
        return clientService.findById(clientId)?.let {
            val mappedClient = it.partialUpdate(dto)
            val updatedInDb = clientService.update(clientId, mappedClient)
            ResponseEntity.ok(updatedInDb.toDto())
        } ?: ResponseEntity.notFound().build()
    }

    @DeleteMapping("/{id}")
    fun deleteById(@PathVariable id: ObjectId): ResponseEntity<Unit> {
        clientService.deleteById(id)
        return ResponseEntity.noContent().build()
    }

}

