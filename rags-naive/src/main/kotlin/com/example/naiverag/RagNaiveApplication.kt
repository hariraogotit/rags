package com.example.naiverag

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.SpringApplication

@SpringBootApplication
class RagNaiveApplication

fun main(args: Array<String>) {
    SpringApplication.run(RagNaiveApplication::class.java, *args)
}
