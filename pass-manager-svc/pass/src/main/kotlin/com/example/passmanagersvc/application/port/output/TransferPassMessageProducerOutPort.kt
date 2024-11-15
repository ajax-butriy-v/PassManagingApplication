package com.example.passmanagersvc.application.port.output

import com.example.passmanagersvc.domain.Pass
import reactor.core.publisher.Mono

interface TransferPassMessageProducerOutPort {
    fun sendTransferPassMessage(updatedPass: Pass, key: String, previousOwnerId: String): Mono<Unit>
}
