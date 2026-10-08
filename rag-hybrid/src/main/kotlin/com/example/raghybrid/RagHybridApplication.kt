package com.example.raghybrid
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan("com.example.raghybrid","com.example.ragcommon")
class RagHybridApplication
fun main(args: Array<String>) {
    runApplication<RagHybridApplication>(*args)
}
