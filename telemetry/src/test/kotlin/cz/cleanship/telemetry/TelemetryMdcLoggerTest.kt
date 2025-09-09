package cz.cleanship.telemetry

import io.opentelemetry.api.common.AttributeKey
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class TelemetryMdcLoggerTest {

    private fun createTelemetry(): DefaultTelemetry = DefaultTelemetry(
        TelemetryConfig(
            serviceName = "mdc-service",
            tracesExporters = setOf(TracesExporter.INMEMORY_FOR_TESTS),
            metricsExporters = emptySet(),
            otlpEndpoint = null,
        ),
    )

    @Test
    fun `shutdown completes without throwing`() {
        // given
        val telemetry = createTelemetry()

        // when/then
        assertDoesNotThrow { telemetry.shutdown() }
    }

    @Test
    fun `prometheusScrape returns null when Prometheus exporter is disabled`() {
        // given
        val telemetry = createTelemetry()

        // when
        val scrape = telemetry.prometheusScrape()

        // then
        assertNull(scrape)
    }

    @Test
    fun `span resource contains service_name`() = runTest {
        // given
        val telemetry = createTelemetry()

        // when
        telemetry.inSpan("res-op") { }

        // then
        val span = telemetry.inMemorySpans().first { it.name == "res-op" }
        assertEquals("mdc-service", span.resource.getAttribute(AttributeKey.stringKey("service.name")))
    }

    @Test
    fun `can start spans for all kinds`() = runTest {
        // given
        val telemetry = createTelemetry()

        // when
        for (k in SpanKind.values()) {
            telemetry.inSpan("kind-$k", k) { }
        }

        // then
        val names = telemetry.inMemorySpans().map { it.name }.toSet()
        for (k in SpanKind.values()) {
            assert(names.contains("kind-$k")) { "missing span for kind $k" }
        }
    }
}
