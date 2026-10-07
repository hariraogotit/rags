package com.example.naiverag

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.boot.SpringApplication

@SpringBootApplication
@ComponentScan("com.example.naiverag","com.example.ragcommon")
class RagNaiveApplication

fun main(args: Array<String>) {
    SpringApplication.run(RagNaiveApplication::class.java, *args)
}
