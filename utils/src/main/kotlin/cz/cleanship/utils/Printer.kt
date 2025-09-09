package cz.cleanship.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
class Printer(
    val message: String,
) {

    /**
     * Test delay in milliseconds.
     */
    private val testDelay = 1000L

    fun printMessage() = runBlocking {
        val now: Instant = Instant.now()
        launch {
            delay(testDelay)
            println(now.toString())
        }
        println(message)
    }
}
