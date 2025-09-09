package cz.cleanship.utils

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PrinterTest {

    @Test
    fun testMessage() {
        // given
        val message = "message"
        // when
        val testPrinter = Printer(message)
        // then
        assertEquals(testPrinter.message, message)
    }

    @Test
    fun testSerialization() {
        // given
        val message = "message"
        // when
        val json1 = Json.encodeToString(Printer(message))
        val json2 = Json.encodeToString(Printer(message))
        // then
        assertEquals(json1, json2)
    }
}
