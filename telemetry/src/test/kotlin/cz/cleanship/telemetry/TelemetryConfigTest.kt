package cz.cleanship.telemetry

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TelemetryConfigTest {

    private companion object {
        private const val SERVICE_NAME_PROP = "telemetry.service.name"
        private const val TRACES_EXPORTER_PROP = "telemetry.traces.exporter"
        private const val METRICS_EXPORTER_PROP = "telemetry.metrics.exporter"
        private const val OTLP_ENDPOINT_PROP = "telemetry.otlp.endpoint"
    }

    private val keys = listOf(
        SERVICE_NAME_PROP,
        TRACES_EXPORTER_PROP,
        METRICS_EXPORTER_PROP,
        OTLP_ENDPOINT_PROP,
    )

    @AfterEach
    fun cleanup() {
        // Clear system properties we might have set to keep tests isolated
        keys.forEach { System.clearProperty(it) }
    }

    @Test
    fun `system properties populate TelemetryConfig fields`() {
        // given
        System.setProperty(SERVICE_NAME_PROP, "svc-A")
        System.setProperty(TRACES_EXPORTER_PROP, "logging,OTLP,inmemory")
        System.setProperty(METRICS_EXPORTER_PROP, "prometheus,logging")
        System.setProperty(OTLP_ENDPOINT_PROP, "http://example:4318")

        // when
        val cfg = TelemetryConfig.fromEnvironment()

        // then
        assertEquals("svc-A", cfg.serviceName)
        assertEquals(setOf(TracesExporter.LOGGING, TracesExporter.OTLP, TracesExporter.INMEMORY_FOR_TESTS), cfg.tracesExporters)
        assertEquals(setOf(MetricsExporter.PROMETHEUS, MetricsExporter.LOGGING), cfg.metricsExporters)
        assertEquals("http://example:4318", cfg.otlpEndpoint)
    }

    @Test
    fun `exporter lists ignore none and unknown tokens`() {
        // given
        System.setProperty(TRACES_EXPORTER_PROP, "none,bad, INMEMORY ")
        System.setProperty(METRICS_EXPORTER_PROP, "NONE, ,otlp,bad")

        // when
        val cfg = TelemetryConfig.fromEnvironment()

        // then
        assertEquals(setOf(TracesExporter.INMEMORY_FOR_TESTS), cfg.tracesExporters)
        assertEquals(setOf(MetricsExporter.OTLP), cfg.metricsExporters)
    }

    @Test
    fun `TracesExporter from is case-insensitive and defaults to NONE`() {
        // given/when/then
        assertEquals(TracesExporter.LOGGING, TracesExporter.from("LoGgInG"))
        assertEquals(TracesExporter.OTLP, TracesExporter.from("otlp"))
        assertEquals(TracesExporter.INMEMORY_FOR_TESTS, TracesExporter.from("INMEMORY"))
        assertEquals(TracesExporter.NONE, TracesExporter.from(null))
        assertEquals(TracesExporter.NONE, TracesExporter.from("unknown"))
    }

    @Test
    fun `TracesExporter fromList parses, trims, deduplicates and filters NONE`() {
        // given
        val input = " logging , otlp , none , , inmemory , LOGGING "

        // when
        val result = TracesExporter.fromList(input)

        // then
        assertEquals(setOf(TracesExporter.LOGGING, TracesExporter.OTLP, TracesExporter.INMEMORY_FOR_TESTS), result)
        assertTrue(TracesExporter.fromList(null).isEmpty())
    }

    @Test
    fun `MetricsExporter from is case-insensitive and defaults to NONE`() {
        // given/when/then
        assertEquals(MetricsExporter.PROMETHEUS, MetricsExporter.from("PrOmEtHeUs"))
        assertEquals(MetricsExporter.OTLP, MetricsExporter.from("otlp"))
        assertEquals(MetricsExporter.LOGGING, MetricsExporter.from("LOGGING"))
        assertEquals(MetricsExporter.NONE, MetricsExporter.from(null))
        assertEquals(MetricsExporter.NONE, MetricsExporter.from("unknown"))
    }

    @Test
    fun `MetricsExporter fromList parses, trims, deduplicates and filters NONE`() {
        // given
        val input = " prometheus , logging , none , , OTLP , LOGGING "

        // when
        val result = MetricsExporter.fromList(input)

        // then
        assertEquals(setOf(MetricsExporter.PROMETHEUS, MetricsExporter.LOGGING, MetricsExporter.OTLP), result)
        assertTrue(MetricsExporter.fromList(null).isEmpty())
    }
}
