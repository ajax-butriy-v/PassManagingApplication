package com.example.internal

object KafkaTopic {
    private const val REQUEST_PREFIX = "com.example.passmanager.output.pub"

    object KafkaTransferPassEvents {
        private const val PASS_PREFIX = "$REQUEST_PREFIX.pass"

        const val TRANSFER = "$PASS_PREFIX.transfer"
        const val TRANSFER_STATISTICS = "$TRANSFER.statistics"
    }
}
