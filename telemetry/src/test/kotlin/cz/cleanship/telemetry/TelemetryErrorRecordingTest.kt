package cz.cleanship.telemetry

import io.opentelemetry.api.trace.StatusCode
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TelemetryErrorRecordingTest {

    private fun createTelemetry(): DefaultTelemetry {
        val cfg = TelemetryConfig(
            serviceName = "test-service",
            tracesExporters = setOf(TracesExporter.INMEMORY_FOR_TESTS),
            metricsExporters = emptySet(),
            otlpEndpoint = null,
        )
        return DefaultTelemetry(cfg)
    }

    @Test
    fun `exceptions inside inSpan are recorded and status set to ERROR`() = runTest {
        // given
        val telemetry = createTelemetry()

        // when
        runCatching {
            telemetry.inSpan("failing-op") {
                throw IllegalStateException("boom")
            }
        }

        // then
        val spans = telemetry.inMemorySpans()
        val span = spans.find { it.name == "failing-op" }
        assertNotNull(span)
        span!!
        assertEquals(StatusCode.ERROR, span.status.statusCode)
        assertTrue(span.events.any { it.name == "exception" }, "span should contain exception event")
    }
}
