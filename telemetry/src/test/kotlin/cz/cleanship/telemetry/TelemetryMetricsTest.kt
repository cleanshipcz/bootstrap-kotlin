package cz.cleanship.telemetry

import io.micrometer.core.instrument.search.Search
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class TelemetryMetricsTest {

    private fun createTelemetry(metricsExporters: Set<MetricsExporter> = emptySet()): DefaultTelemetry {
        val cfg = TelemetryConfig(
            serviceName = "test-service",
            tracesExporters = setOf(TracesExporter.INMEMORY_FOR_TESTS),
            metricsExporters = metricsExporters,
            otlpEndpoint = null,
        )
        return DefaultTelemetry(cfg)
    }

    @Test
    fun `counter increments with tags`() {
        // given
        // - counter with a static label
        val telemetry = createTelemetry()
        val counter = telemetry.counter("test_counter", mapOf("label" to "value"))

        // when
        counter.increment(2.0)
        counter.increment(3.0)

        // then
        // - micrometer registry observes the expected total
        val found = Search
            .`in`(telemetry.registry())
            .name("test_counter")
            .tag("label", "value")
            .counter()
        assertNotNull(found)
        assertEquals(5.0, found!!.count(), 1e-6)
    }

    @Test
    fun `timer records durations`() = runTest {
        // given
        // - a timer with a label for filtering
        val telemetry = createTelemetry()
        val timer = telemetry.timer("test_timer", mapOf("status" to "ok"))

        // when
        timer.record(25)
        timer.recordSuspend { /* simulate work */ }

        // then
        // - count >= 2 and total time is positive
        val found = Search
            .`in`(telemetry.registry())
            .name("test_timer")
            .tag("status", "ok")
            .timer()
        assertNotNull(found)
        assertTrue(found!!.count() >= 2L)
        assertTrue(found.totalTime(TimeUnit.NANOSECONDS) > 0.0)
    }

    @Test
    fun `gauge reflects last value`() {
        // given
        // - gauge updated over time
        val telemetry = createTelemetry()
        val gauge = telemetry.gauge("test_gauge", mapOf("phase" to "alpha"))

        // when
        gauge.set(7.0)

        // then
        // - registry shows the latest value
        val found = Search
            .`in`(telemetry.registry())
            .name("test_gauge")
            .tag("phase", "alpha")
            .gauge()
        assertNotNull(found)
        assertEquals(7.0, found!!.value(), 1e-6)

        // when
        // - update to a new value
        gauge.set(2.5)

        // then
        // - registry reflects the last set value
        assertEquals(2.5, found.value(), 1e-6)
    }
}
