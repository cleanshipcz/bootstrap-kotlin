package cz.cleanship.telemetry

import io.micrometer.core.instrument.search.Search
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class TelemetryMetricsTest {

    private fun createTelemetry(metricsExporter: MetricsExporter = MetricsExporter.NONE): DefaultTelemetry {
        val cfg = TelemetryConfig(
            serviceName = "test-service",
            tracesExporter = TracesExporter.INMEMORY_FOR_TESTS,
            metricsExporter = metricsExporter,
            otlpEndpoint = null
        )
        return DefaultTelemetry(cfg)
    }

    @Test
    fun `counter increments with tags`() {
        val telemetry = createTelemetry()
        val counter = telemetry.counter("test_counter", mapOf("label" to "value"))
        counter.increment(2.0)
        counter.increment(3.0)

        val found = Search.`in`(telemetry.registry()).name("test_counter").tag("label", "value").counter()
        assertNotNull(found)
        assertEquals(5.0, found!!.count(), 1e-6)
    }

    @Test
    fun `timer records durations`() = runTest {
        val telemetry = createTelemetry()
        val timer = telemetry.timer("test_timer", mapOf("status" to "ok"))
        timer.record(25)
        timer.recordSuspend { /* work */ }

        val found = Search.`in`(telemetry.registry()).name("test_timer").tag("status", "ok").timer()
        assertNotNull(found)
        assertTrue(found!!.count() >= 2L)
        assertTrue(found.totalTime(TimeUnit.NANOSECONDS) > 0.0)
    }

    @Test
    fun `gauge reflects last value`() {
        val telemetry = createTelemetry()
        val gauge = telemetry.gauge("test_gauge", mapOf("phase" to "alpha"))
        gauge.set(7.0)
        val found = Search.`in`(telemetry.registry()).name("test_gauge").tag("phase", "alpha").gauge()
        assertNotNull(found)
        assertEquals(7.0, found!!.value(), 1e-6)
        gauge.set(2.5)
        assertEquals(2.5, found.value(), 1e-6)
    }
}
