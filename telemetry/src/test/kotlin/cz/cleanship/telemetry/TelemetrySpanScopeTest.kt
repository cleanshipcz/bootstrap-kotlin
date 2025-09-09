package cz.cleanship.telemetry

import cz.cleanship.telemetry.support.LocalInMemorySpanExporter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TelemetrySpanScopeTest {

    private fun createTelemetry(): DefaultTelemetry {
        val cfg = TelemetryConfig(
            serviceName = "test-service",
            tracesExporters = setOf(TracesExporter.INMEMORY_FOR_TESTS),
            metricsExporters = emptySet(),
            otlpEndpoint = null,
        )
        return DefaultTelemetry(cfg, LocalInMemorySpanExporter())
    }

    @Test
    fun `startSpan creates and ends span on close`() {
        // given
        // - a telemetry instance with in-memory exporter for assertions
        val telemetry = createTelemetry()

        // when
        // - start a span using startSpan() and close the scope to end it
        val scope = telemetry.startSpan("manual-span", SpanKind.INTERNAL)
        scope.close()

        // then
        // - a span with the given name was exported
        val spans = telemetry.inMemorySpans()
        val span = spans.find { it.name == "manual-span" }!!
        assertEquals("manual-span", span.name)
    }
}
