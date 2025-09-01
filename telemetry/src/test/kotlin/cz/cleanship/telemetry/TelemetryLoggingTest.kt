package cz.cleanship.telemetry

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class TelemetryLoggingTest {

    private fun createTelemetry(): DefaultTelemetry {
        val cfg = TelemetryConfig(
            serviceName = "test-service",
            tracesExporters = setOf(TracesExporter.INMEMORY_FOR_TESTS),
            metricsExporters = emptySet(),
            otlpEndpoint = null
        )
        return DefaultTelemetry(cfg)
    }

    @Test
    fun `logs inside spans include trace and span ids via MDC`() = runTest {
        val telemetry = createTelemetry()
        val loggerName = "telemetry.test"

        val slf4jLogger = LoggerFactory.getLogger(loggerName) as Logger
        val appender = ListAppender<ILoggingEvent>()
        appender.start()
        slf4jLogger.addAppender(appender)

        telemetry.inSpan("logging-span") { _ ->
            telemetry.logger(loggerName).info("hello", mapOf("foo" to "bar"))
        }

        val events = appender.list
        assertTrue(events.isNotEmpty())
        val event = events.last()

        val mdc = event.mdcPropertyMap
        val traceId = mdc["trace_id"]
        val spanId = mdc["span_id"]
        assertNotNull(traceId)
        assertNotNull(spanId)
    }
}
