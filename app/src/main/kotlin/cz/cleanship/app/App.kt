package cz.cleanship.app

import cz.cleanship.telemetry.Telemetry
import cz.cleanship.telemetry.logging.info
import cz.cleanship.utils.Calculator
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val log: Logger = LoggerFactory.getLogger("App")
val telemetry = Telemetry.create()
val logCounter = telemetry.counter("LogCounter")

fun main() = runBlocking {
    telemetry.inSpan("main") {
        log.info("Start")
        val calculator = Calculator()
        val a = 7
        for (b in 1..5) {
            logCounter.increment()
            log.info(
                "Result of ($a + $b) = ${calculator.add(a, b)}",
                fields = mapOf("result" to calculator.add(a, b)),
            )
        }
        log.info("Done")
    }
}
