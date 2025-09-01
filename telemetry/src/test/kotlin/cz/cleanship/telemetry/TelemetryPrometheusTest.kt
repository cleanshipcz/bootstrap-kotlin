package cz.cleanship.telemetry

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TelemetryPrometheusTest {

    private fun createTelemetry(): DefaultTelemetry {
        val cfg = TelemetryConfig(
            serviceName = "test-service",
            tracesExporters = setOf(TracesExporter.INMEMORY_FOR_TESTS),
            metricsExporters = setOf(MetricsExporter.PROMETHEUS),
            otlpEndpoint = null,
        )
        return DefaultTelemetry(cfg)
    }

    @Test
    fun `prometheus scrape includes metrics`() {
        val telemetry = createTelemetry()
        val counter = telemetry.counter("http_server_requests_total", mapOf("method" to "GET", "status" to "200"))
        counter.increment()

        val scrape = telemetry.prometheusScrape()
        assertNotNull(scrape)
        val text = scrape!!
        assertTrue(text.contains("http_server_requests_total"))
        assertTrue(text.contains("method=\"GET\""))
        assertTrue(text.contains("status=\"200\""))
    }
}
