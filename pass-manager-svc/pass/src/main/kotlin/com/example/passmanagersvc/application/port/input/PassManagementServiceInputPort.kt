package com.example.passmanagersvc.application.port.input

import reactor.core.publisher.Mono

interface PassManagementServiceInputPort {
    fun cancelPass(passOwnerId: String, passId: String): Mono<Unit>
    fun transferPass(passId: String, targetPassOwnerId: String): Mono<Unit>
}