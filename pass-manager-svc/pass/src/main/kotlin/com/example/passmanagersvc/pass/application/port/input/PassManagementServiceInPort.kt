package com.example.passmanagersvc.pass.application.port.input

import com.example.passmanagersvc.pass.domain.Pass
import reactor.core.publisher.Mono

interface PassManagementServiceInPort {
    fun cancelPass(passOwnerId: String, passId: String): Mono<Unit>
    fun transferPass(passId: String, targetPassOwnerId: String): Mono<Unit>
    fun publishTransferPassStatistics(pass: Pass, previousPassOwnerId: String): Mono<Unit>
}
