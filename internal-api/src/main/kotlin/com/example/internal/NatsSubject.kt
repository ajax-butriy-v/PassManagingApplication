package com.example.internal

object NatsSubject {
    private const val REQUEST_PREFIX = "com.example.passmanager.input.request"

    object Pass {
        private const val PASS_PREFIX = "$REQUEST_PREFIX.pass"

        const val FIND_BY_ID = "$PASS_PREFIX.find_by_id"
        const val CREATE = "$PASS_PREFIX.create"
        const val CANCEL = "$PASS_PREFIX.cancel"
        const val TRANSFER = "$PASS_PREFIX.transfer"
        const val DELETE_BY_ID = "$PASS_PREFIX.delete_by_id"
    }
}
