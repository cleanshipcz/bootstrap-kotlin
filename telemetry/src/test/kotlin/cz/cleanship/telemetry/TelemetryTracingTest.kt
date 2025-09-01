package cz.cleanship.telemetry

import io.opentelemetry.api.common.AttributeKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TelemetryTracingTest {

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
    fun `parent-child spans are linked`() = runTest {
        val telemetry = createTelemetry()

        telemetry.inSpan("parent", SpanKind.INTERNAL) { parent ->
            telemetry.inSpan("child", SpanKind.INTERNAL) { child ->
                // no-op
            }
        }

        val spans = telemetry.inMemorySpans()
        val parent = spans.find { it.name == "parent" }!!
        val child = spans.find { it.name == "child" }!!

        assertEquals(parent.traceId, child.traceId)
        assertEquals(parent.spanId, child.parentSpanId)
    }

    @Test
    fun `trace context propagates across coroutines`() = runTest {
        val telemetry = createTelemetry()

        telemetry.inSpan("root", SpanKind.INTERNAL) { root ->
            withContext(Dispatchers.Default) {
                telemetry.inSpan("async-child") { child ->
                    // nothing
                }
            }
        }

        val spans = telemetry.inMemorySpans()
        val root = spans.find { it.name == "root" }!!
        val child = spans.find { it.name == "async-child" }!!
        assertEquals(root.traceId, child.traceId)
        assertEquals(root.spanId, child.parentSpanId)
    }

    @Test
    fun `attributes are set on spans`() = runTest {
        val telemetry = createTelemetry()
        telemetry.inSpan("operation", attributes = mapOf("user.id" to 123, "success" to true)) { span ->
            // nothing
        }
        val span = telemetry.inMemorySpans().find { it.name == "operation" }!!
        assertEquals(123L, span.attributes.get(AttributeKey.longKey("user.id")))
        assertEquals(true, span.attributes.get(AttributeKey.booleanKey("success")))
    }
}
