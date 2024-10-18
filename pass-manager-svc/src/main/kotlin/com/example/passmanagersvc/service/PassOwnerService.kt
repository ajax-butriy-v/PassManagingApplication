package com.example.passmanagersvc.service

import com.example.passmanagersvc.domain.MongoPassOwner
import com.example.passmanagersvc.web.dto.PassOwnerUpdateDto
import reactor.core.publisher.Mono

interface PassOwnerService {
    fun findById(passOwnerId: String): Mono<MongoPassOwner>
    fun getById(passOwnerId: String): Mono<MongoPassOwner>
    fun create(newMongoPassOwner: MongoPassOwner): Mono<MongoPassOwner>
    fun update(passOwnerId: String, passOwnerUpdateDto: PassOwnerUpdateDto): Mono<MongoPassOwner>
    fun deleteById(passOwnerId: String): Mono<Unit>
}
