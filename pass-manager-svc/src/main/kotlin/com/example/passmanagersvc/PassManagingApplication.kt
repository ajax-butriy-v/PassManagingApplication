package com.example.passmanagersvc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PassManagingApplication

@SuppressWarnings("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<PassManagingApplication>(*args)
}
