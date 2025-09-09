package cz.cleanship.telemetry

import io.opentelemetry.api.common.AttributeKey
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TelemetryAttributeTypesTest {

    private fun telemetry(): DefaultTelemetry = DefaultTelemetry(
        TelemetryConfig(
            serviceName = "attr-service",
            tracesExporters = setOf(TracesExporter.INMEMORY_FOR_TESTS),
            metricsExporters = emptySet(),
            otlpEndpoint = null,
        ),
    )

    @Test
    fun `attributes of various types are set with correct typing`() = runTest {
        // given
        val t = telemetry()

        // when
        t.inSpan(
            name = "attr-op",
            attributes = mapOf(
                "s" to "str",
                "b" to true,
                "i" to 7,
                "l" to 9L,
                "d" to 1.5,
                "f" to 2.5f,
                "x" to listOf(1, 2, 3), // fallback to string
            ),
        ) { }

        // then
        val span = t.inMemorySpans().find { it.name == "attr-op" }!!
        val attrs = span.attributes
        assertEquals("str", attrs.get(AttributeKey.stringKey("s")))
        assertEquals(true, attrs.get(AttributeKey.booleanKey("b")))
        assertEquals(7L, attrs.get(AttributeKey.longKey("i")))
        assertEquals(9L, attrs.get(AttributeKey.longKey("l")))
        assertEquals(1.5, attrs.get(AttributeKey.doubleKey("d")))
        assertEquals(2.5, attrs.get(AttributeKey.doubleKey("f")))
        assertEquals(listOf(1, 2, 3).toString(), attrs.get(AttributeKey.stringKey("x")))
    }
}
