package com.example.passmanager.web.controller

import com.example.passmanager.service.PassOwnerService
import com.example.passmanager.service.PassOwnerStatisticsService
import com.example.passmanager.web.dto.PassOwnerDto
import com.example.passmanager.web.dto.PriceDistribution
import com.example.passmanager.web.dto.SpentAfterDateDto
import com.example.passmanager.web.mapper.PassOwnerMapper.partialUpdate
import com.example.passmanager.web.mapper.PassOwnerMapper.toDto
import com.example.passmanager.web.mapper.PassOwnerMapper.toEntity
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
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

@RestController
@RequestMapping("/owners")
internal class PassOwnerController(
    private val passOwnerService: PassOwnerService,
    private val passOwnerStatisticsService: PassOwnerStatisticsService,
) {

    @GetMapping("/{id}/distributions")
    fun calculatePriceDistributions(@PathVariable id: String): ResponseEntity<Flux<PriceDistribution>> {
        return ResponseEntity.ok(passOwnerStatisticsService.calculatePriceDistributions(id))
    }

    @GetMapping("/{id}/spent")
    fun calculateSpentAfterDate(
        @RequestParam afterDate: LocalDate,
        @PathVariable("id") ownerId: String,
    ): Mono<ResponseEntity<SpentAfterDateDto>> {
        return passOwnerStatisticsService.calculateSpentAfterDate(afterDate, ownerId)
            .map { spentAfterDate -> SpentAfterDateDto(ownerId, afterDate, spentAfterDate) }
            .map { ResponseEntity.ok(it) }
    }

    @PostMapping
    fun create(@Valid @RequestBody dto: PassOwnerDto): Mono<ResponseEntity<PassOwnerDto>> {
        return passOwnerService.create(dto.toEntity())
            .map { it.toDto() }
            .map { ResponseEntity.status(HttpStatus.CREATED).body(it) }
    }

    @PatchMapping("/{id}")
    fun partialUpdate(
        @Valid @RequestBody dto: PassOwnerDto,
        @PathVariable("id") ownerId: String,
    ): Mono<ResponseEntity<PassOwnerDto>> {
        return passOwnerService.getById(ownerId)
            .map { passOwner -> passOwner.partialUpdate(dto) }
            .flatMap { partiallyUpdated -> passOwnerService.update(partiallyUpdated) }
            .map { it.toDto() }
            .map { ResponseEntity.ok(it) }
    }

    @DeleteMapping("/{id}")
    fun deleteById(@PathVariable id: String): Mono<ResponseEntity<Unit>> {
        return passOwnerService.deleteById(id).thenReturn(ResponseEntity.noContent().build())
    }
}
