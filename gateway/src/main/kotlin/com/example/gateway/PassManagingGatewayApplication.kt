package com.example.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
internal class PassManagingGatewayApplication

@SuppressWarnings("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<PassManagingGatewayApplication>(*args)
}
