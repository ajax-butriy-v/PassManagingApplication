package com.example.passmanagersvc.service

import reactor.core.publisher.Mono

interface PassManagementService {
    fun cancelPass(passOwnerId: String, passId: String): Mono<Unit>
    fun transferPass(passId: String, targetPassOwnerId: String): Mono<Unit>
}
