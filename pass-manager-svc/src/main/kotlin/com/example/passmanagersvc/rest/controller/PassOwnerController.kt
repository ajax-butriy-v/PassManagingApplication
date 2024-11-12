package com.example.passmanagersvc.rest.controller

import com.example.passmanagersvc.dto.PassOwnerDto
import com.example.passmanagersvc.dto.PassOwnerUpdateDto
import com.example.passmanagersvc.dto.PriceDistribution
import com.example.passmanagersvc.dto.SpentAfterDateDto
import com.example.passmanagersvc.mapper.PassOwnerMapper.toDto
import com.example.passmanagersvc.mapper.PassOwnerMapper.toEntity
import com.example.passmanagersvc.service.PassOwnerService
import com.example.passmanagersvc.service.PassOwnerStatisticsService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

@RestController
@RequestMapping("/owners")
class PassOwnerController(
    private val passOwnerService: PassOwnerService,
    private val passOwnerStatisticsService: PassOwnerStatisticsService,
) {

    @GetMapping("/{id}/distributions")
    @ResponseStatus(HttpStatus.OK)
    fun calculatePriceDistributions(@PathVariable id: String): Flux<PriceDistribution> {
        return passOwnerStatisticsService.calculatePriceDistributions(id)
    }

    @GetMapping("/{id}/spent")
    @ResponseStatus(HttpStatus.OK)
    fun calculateSpentAfterDate(
        @RequestParam afterDate: LocalDate,
        @PathVariable("id") ownerId: String,
    ): Mono<SpentAfterDateDto> {
        return passOwnerStatisticsService.calculateSpentAfterDate(afterDate, ownerId)
            .map { spentAfterDate -> SpentAfterDateDto(ownerId, afterDate, spentAfterDate) }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody dto: PassOwnerDto): Mono<PassOwnerDto> {
        return passOwnerService.create(dto.toEntity()).map { it.toDto() }
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun partialUpdate(
        @Valid @RequestBody updateDto: PassOwnerUpdateDto,
        @PathVariable("id") ownerId: String,
    ): Mono<PassOwnerDto> {
        return passOwnerService.update(ownerId, updateDto).map { it.toDto() }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteById(@PathVariable id: String): Mono<Unit> {
        return passOwnerService.deleteById(id)
    }
}
