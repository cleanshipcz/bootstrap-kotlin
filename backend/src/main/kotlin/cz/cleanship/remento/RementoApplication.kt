package cz.cleanship.remento

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class RementoApplication

fun main(args: Array<String>) {
    @Suppress("SpreadOperator")
    runApplication<RementoApplication>(*args)
}
