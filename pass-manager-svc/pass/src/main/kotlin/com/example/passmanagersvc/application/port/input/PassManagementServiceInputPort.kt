package com.example.passmanagersvc.application.port.input

import com.example.passmanagersvc.domain.Pass
import reactor.core.publisher.Mono

interface PassManagementServiceInputPort {
    fun cancelPass(passOwnerId: String, passId: String): Mono<Unit>
    fun transferPass(passId: String, targetPassOwnerId: String): Mono<Unit>
    fun publishTransferPassStatistics(pass: Pass, previousPassOwnerId: String): Mono<Unit>
}
