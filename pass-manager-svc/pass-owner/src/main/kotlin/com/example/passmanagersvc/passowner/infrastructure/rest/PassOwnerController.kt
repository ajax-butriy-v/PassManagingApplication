package com.example.passmanagersvc.passowner.infrastructure.rest

import com.example.passmanagersvc.passowner.application.port.input.PassOwnerServiceInPort
import com.example.passmanagersvc.passowner.application.port.input.PassOwnerStatisticsServiceInPort
import com.example.passmanagersvc.passowner.domain.PriceDistribution
import com.example.passmanagersvc.passowner.infrastructure.rest.dto.PassOwnerDto
import com.example.passmanagersvc.passowner.infrastructure.rest.dto.PassOwnerUpdateDto
import com.example.passmanagersvc.passowner.infrastructure.rest.dto.SpentAfterDateDto
import com.example.passmanagersvc.passowner.infrastructure.rest.mapper.PassOwnerMapper.partialUpdate
import com.example.passmanagersvc.passowner.infrastructure.rest.mapper.PassOwnerMapper.toDomain
import com.example.passmanagersvc.passowner.infrastructure.rest.mapper.PassOwnerMapper.toDto
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
    private val passOwnerServiceInPort: PassOwnerServiceInPort,
    private val passOwnerStatisticsServiceInPort: PassOwnerStatisticsServiceInPort,
) {

    @GetMapping("/{id}/distributions")
    @ResponseStatus(HttpStatus.OK)
    fun calculatePriceDistributions(@PathVariable id: String): Flux<PriceDistribution> {
        return passOwnerStatisticsServiceInPort.calculatePriceDistributions(id)
    }

    @GetMapping("/{id}/spent")
    @ResponseStatus(HttpStatus.OK)
    fun calculateSpentAfterDate(
        @RequestParam afterDate: LocalDate,
        @PathVariable("id") ownerId: String,
    ): Mono<SpentAfterDateDto> {
        return passOwnerStatisticsServiceInPort.calculateSpentAfterDate(afterDate, ownerId)
            .map { spentAfterDate -> SpentAfterDateDto(ownerId, afterDate, spentAfterDate) }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody dto: PassOwnerDto): Mono<PassOwnerDto> {
        return passOwnerServiceInPort.create(dto.toDomain()).map { it.toDto() }
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun partialUpdate(
        @Valid @RequestBody updateDto: PassOwnerUpdateDto,
        @PathVariable("id") ownerId: String,
    ): Mono<PassOwnerDto> {
        return passOwnerServiceInPort.getById(ownerId)
            .map { it.partialUpdate(updateDto) }
            .flatMap { passOwnerServiceInPort.update(ownerId, it) }
            .map { it.toDto() }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteById(@PathVariable id: String): Mono<Unit> {
        return passOwnerServiceInPort.deleteById(id)
    }
}
