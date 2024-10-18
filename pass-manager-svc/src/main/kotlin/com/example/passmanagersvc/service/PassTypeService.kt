package com.example.passmanagersvc.service

import com.example.passmanagersvc.domain.MongoPassType
import reactor.core.publisher.Mono

interface PassTypeService {
    fun findById(id: String): Mono<MongoPassType>
    fun getById(id: String): Mono<MongoPassType>
    fun create(passType: MongoPassType): Mono<MongoPassType>
    fun update(modifiedPassType: MongoPassType): Mono<MongoPassType>
    fun deleteById(id: String): Mono<Unit>
}
