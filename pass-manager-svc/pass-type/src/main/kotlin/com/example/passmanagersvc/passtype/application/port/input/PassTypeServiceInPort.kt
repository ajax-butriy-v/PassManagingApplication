package com.example.passmanagersvc.passtype.application.port.input

import com.example.passmanagersvc.passtype.domain.PassType
import reactor.core.publisher.Mono

interface PassTypeServiceInPort {
    fun findById(id: String): Mono<PassType>
    fun getById(id: String): Mono<PassType>
    fun create(passType: PassType): Mono<PassType>
    fun update(modifiedPassType: PassType): Mono<PassType>
    fun deleteById(id: String): Mono<Unit>
}
