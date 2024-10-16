package com.example.passmanagersvc

import io.mongock.runner.springboot.EnableMongock
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableMongock
internal class PassManagingApplication

@SuppressWarnings("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<PassManagingApplication>(*args)
}
