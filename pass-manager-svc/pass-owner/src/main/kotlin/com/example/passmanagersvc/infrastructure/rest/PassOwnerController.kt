package com.example.passmanagersvc.infrastructure.rest

import com.example.passmanagersvc.application.port.input.PassOwnerServiceInputPort
import com.example.passmanagersvc.application.port.input.PassOwnerStatisticsServiceInputPort
import com.example.passmanagersvc.domain.PriceDistribution
import com.example.passmanagersvc.infrastructure.rest.dto.PassOwnerDto
import com.example.passmanagersvc.infrastructure.rest.dto.PassOwnerUpdateDto
import com.example.passmanagersvc.infrastructure.rest.dto.SpentAfterDateDto
import com.example.passmanagersvc.infrastructure.rest.mapper.PassOwnerMapper.partialUpdate
import com.example.passmanagersvc.infrastructure.rest.mapper.PassOwnerMapper.toDomain
import com.example.passmanagersvc.infrastructure.rest.mapper.PassOwnerMapper.toDto
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
    private val passOwnerServiceInputPort: PassOwnerServiceInputPort,
    private val passOwnerStatisticsServiceInputPort: PassOwnerStatisticsServiceInputPort,
) {

    @GetMapping("/{id}/distributions")
    @ResponseStatus(HttpStatus.OK)
    fun calculatePriceDistributions(@PathVariable id: String): Flux<PriceDistribution> {
        return passOwnerStatisticsServiceInputPort.calculatePriceDistributions(id)
    }

    @GetMapping("/{id}/spent")
    @ResponseStatus(HttpStatus.OK)
    fun calculateSpentAfterDate(
        @RequestParam afterDate: LocalDate,
        @PathVariable("id") ownerId: String,
    ): Mono<SpentAfterDateDto> {
        return passOwnerStatisticsServiceInputPort.calculateSpentAfterDate(afterDate, ownerId)
            .map { spentAfterDate -> SpentAfterDateDto(ownerId, afterDate, spentAfterDate) }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody dto: PassOwnerDto): Mono<PassOwnerDto> {
        return passOwnerServiceInputPort.create(dto.toDomain()).map { it.toDto() }
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun partialUpdate(
        @Valid @RequestBody updateDto: PassOwnerUpdateDto,
        @PathVariable("id") ownerId: String,
    ): Mono<PassOwnerDto> {
        return passOwnerServiceInputPort.getById(ownerId)
            .map { it.partialUpdate(updateDto) }
            .flatMap { passOwnerServiceInputPort.update(ownerId, it) }
            .map { it.toDto() }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteById(@PathVariable id: String): Mono<Unit> {
        return passOwnerServiceInputPort.deleteById(id)
    }
}
