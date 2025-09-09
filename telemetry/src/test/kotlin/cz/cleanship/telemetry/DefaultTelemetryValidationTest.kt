package cz.cleanship.telemetry

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class DefaultTelemetryValidationTest {

    private fun telemetry(): DefaultTelemetry = DefaultTelemetry(
        TelemetryConfig(
            serviceName = "test-service",
            tracesExporters = setOf(TracesExporter.INMEMORY_FOR_TESTS),
            metricsExporters = emptySet(),
            otlpEndpoint = null,
        ),
    )

    @Test
    fun `metric name must match regex`() {
        // given
        val telemetry = telemetry()

        // when/then
        assertThrows(IllegalArgumentException::class.java) { telemetry.counter("bad-name") }
        assertThrows(IllegalArgumentException::class.java) { telemetry.timer("db.query.latency") }
        assertDoesNotThrow { telemetry.counter("good_name:part2") }
    }

    @Test
    fun `label key and value must follow rules`() {
        // given
        val telemetry = telemetry()

        // when/then
        assertThrows(IllegalArgumentException::class.java) { telemetry.counter("ok", mapOf("bad key" to "v")) }
        assertThrows(IllegalArgumentException::class.java) { telemetry.counter("ok", mapOf("key" to "bad value")) }
        assertThrows(IllegalArgumentException::class.java) { telemetry.gauge("ok", mapOf("http.route" to "/hello")) }
        assertDoesNotThrow { telemetry.timer("ok", mapOf("status_code" to "200")) }
    }
}
