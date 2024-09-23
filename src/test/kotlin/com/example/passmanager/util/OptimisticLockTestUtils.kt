package com.example.passmanager.util

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

object OptimisticLockTestUtils {
    fun getOptimisticLocksAmount(tasks: List<Runnable>): Int {
        val optimisticLocks = AtomicInteger(0)
        val executorService = Executors.newFixedThreadPool(tasks.size)

        tasks.forEach { task ->
            executorService.execute {
                runCatching { task.run() }.onFailure { optimisticLocks.incrementAndGet() }
            }
        }
        executorService.apply {
            shutdown()
            awaitTermination(3, TimeUnit.SECONDS)
        }
        return optimisticLocks.get()
    }
}
