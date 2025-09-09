package cz.cleanship.telemetry

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.OutputStreamAppender
import cz.cleanship.telemetry.logging.TraceJsonProvider
import kotlinx.coroutines.test.runTest
import net.logstash.logback.composite.loggingevent.LoggingEventJsonProviders
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream

class TelemetryLoggingTest {

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
    fun `logs inside spans include trace and span ids via TraceJsonProvider`() = runTest {
        // given
        // - configure Logback JSON with TraceJsonProvider and capture output
        val telemetry = createTelemetry()
        val loggerName = "telemetry.test"

        val slf4jLogger = LoggerFactory.getLogger(loggerName) as Logger
        val ctx = slf4jLogger.loggerContext

        // Configure JSON encoder with our provider
        val encoder = LoggingEventCompositeJsonEncoder()
        encoder.context = ctx
        val providers = LoggingEventJsonProviders()
        providers.addProvider(TraceJsonProvider())
        encoder.setProviders(providers)
        encoder.start()

        // Capture output into a byte array
        val baos = ByteArrayOutputStream()
        val appender = object : OutputStreamAppender<ILoggingEvent>() {}
        appender.context = ctx
        appender.encoder = encoder
        appender.outputStream = baos
        appender.start()
        slf4jLogger.addAppender(appender)

        // when
        // - log inside an active span
        telemetry.inSpan("logging-span") { _ ->
            slf4jLogger.atInfo().addKeyValue("foo", "bar").log("hello")
        }

        // then
        // - JSON contains trace/span identifiers
        val json = baos.toString("UTF-8")
        assertTrue(json.contains("trace_id"), "JSON should contain trace_id")
        assertTrue(json.contains("span_id"), "JSON should contain span_id")
    }
}
