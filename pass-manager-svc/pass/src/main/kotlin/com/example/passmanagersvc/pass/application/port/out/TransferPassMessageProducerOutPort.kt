package com.example.passmanagersvc.pass.application.port.out

import com.example.passmanagersvc.pass.domain.Pass
import reactor.core.publisher.Mono

interface TransferPassMessageProducerOutPort {
    fun sendTransferPassMessage(updatedPass: Pass, key: String, previousOwnerId: String): Mono<Unit>
}
