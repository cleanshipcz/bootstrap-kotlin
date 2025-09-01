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
            otlpEndpoint = null
        )
        return DefaultTelemetry(cfg, LocalInMemorySpanExporter())
    }

    @Test
    fun `startSpan creates and ends span on close`() {
        val telemetry = createTelemetry()
        val scope = telemetry.startSpan("manual-span", SpanKind.INTERNAL)
        scope.close()

        val spans = telemetry.inMemorySpans()
        val span = spans.find { it.name == "manual-span" }!!
        assertEquals("manual-span", span.name)
    }
}
