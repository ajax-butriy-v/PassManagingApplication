package com.example.passmanager

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
internal class PassManagingApplication

@SuppressWarnings("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<PassManagingApplication>(*args)
}
