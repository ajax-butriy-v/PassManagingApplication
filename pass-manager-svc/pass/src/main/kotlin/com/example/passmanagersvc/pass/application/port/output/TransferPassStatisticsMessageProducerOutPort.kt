package com.example.passmanagersvc.pass.application.port.output

import com.example.internal.input.reqreply.TransferredPassStatisticsMessage
import reactor.core.publisher.Mono

interface TransferPassStatisticsMessageProducerOutPort {
    fun sendTransferPassStatisticsMessage(message: TransferredPassStatisticsMessage, passTypeId: String): Mono<Unit>
}
