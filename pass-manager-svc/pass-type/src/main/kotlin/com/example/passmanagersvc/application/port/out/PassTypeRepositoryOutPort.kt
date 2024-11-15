package com.example.passmanagersvc.application.port.out

import com.example.passmanagersvc.domain.PassType
import reactor.core.publisher.Mono

interface PassTypeRepositoryOutPort {
    fun findById(passTypeId: String): Mono<PassType>
    fun insert(newPassType: PassType): Mono<PassType>
    fun save(modifiedPassType: PassType): Mono<PassType>
    fun deleteById(passTypeId: String): Mono<Unit>
}