package com.example.pass_manager.web

import com.example.pass_manager.service.PassOwnerService
import com.example.pass_manager.service.PassOwnerStatisticsService
import com.example.pass_manager.web.dto.PassOwnerDto
import com.example.pass_manager.web.dto.PriceDistribution
import com.example.pass_manager.web.dto.SpentAfterDateDto
import com.example.pass_manager.web.mapper.PassOwnerMapper.partialUpdate
import com.example.pass_manager.web.mapper.PassOwnerMapper.toDto
import com.example.pass_manager.web.mapper.PassOwnerMapper.toEntity
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/clients")
class PassOwnerController(
    private val passOwnerService: PassOwnerService,
    private val passOwnerStatisticsService: PassOwnerStatisticsService,
) {

    @GetMapping("/{id}/distributions")
    fun calculatePriceDistributions(@PathVariable id: String): ResponseEntity<List<PriceDistribution>> {
        val priceDistributions = passOwnerStatisticsService.calculatePriceDistributions(id.toObjectId())
        return ResponseEntity.ok(priceDistributions)
    }

    @GetMapping("/{id}/spent")
    fun calculateSpentAfterDate(
        @RequestParam afterDate: Instant,
        @PathVariable("id") clientId: String,
    ): ResponseEntity<SpentAfterDateDto> {
        val totalSpent = passOwnerStatisticsService.calculateSpentAfterDate(afterDate, clientId.toObjectId())
        return ResponseEntity.ok(SpentAfterDateDto(clientId, afterDate, totalSpent))
    }

    @PostMapping
    fun create(@Valid @RequestBody dto: PassOwnerDto): ResponseEntity<PassOwnerDto> {
        val created = passOwnerService.create(dto.toEntity())
        return ResponseEntity.status(HttpStatus.CREATED).body(created.toDto())
    }

    @PatchMapping("/{id}")
    fun partialUpdate(
        @Valid @RequestBody dto: PassOwnerDto,
        @PathVariable("id") clientId: String,
    ): ResponseEntity<PassOwnerDto> {
        return passOwnerService.findById(clientId.toObjectId())?.let {
            val mappedClient = it.partialUpdate(dto)
            val updatedInDb = passOwnerService.update(clientId.toObjectId(), mappedClient)
            ResponseEntity.ok(updatedInDb.toDto())
        } ?: ResponseEntity.notFound().build()
    }

    @DeleteMapping("/{id}")
    fun deleteById(@PathVariable id: String): ResponseEntity<Unit> {
        passOwnerService.deleteById(id.toObjectId())
        return ResponseEntity.noContent().build()
    }

}

