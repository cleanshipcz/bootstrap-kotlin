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
        // given
        // - in-memory exporter for assertions
        val telemetry = createTelemetry()

        // when
        // - create a parent span and a nested child span
        telemetry.inSpan("parent", SpanKind.INTERNAL) { _ ->
            telemetry.inSpan("child", SpanKind.INTERNAL) { _ ->
                // - no-op; relationship is what we validate
            }
        }

        // then
        // - child shares traceId and links to parent via parentSpanId
        val spans = telemetry.inMemorySpans()
        val parent = spans.find { it.name == "parent" }!!
        val child = spans.find { it.name == "child" }!!
        assertEquals(parent.traceId, child.traceId)
        assertEquals(parent.spanId, child.parentSpanId)
    }

    @Test
    fun `trace context propagates across coroutines`() = runTest {
        // given
        // - context propagates via asContextElement()
        val telemetry = createTelemetry()

        // when
        // - start a root span and jump to another dispatcher; create a child span there
        telemetry.inSpan("root", SpanKind.INTERNAL) { _ ->
            withContext(Dispatchers.Default) {
                telemetry.inSpan("async-child") { _ ->
                    // - body intentionally empty
                }
            }
        }

        // then
        // - child keeps trace and links to root across dispatcher boundary
        val spans = telemetry.inMemorySpans()
        val root = spans.find { it.name == "root" }!!
        val child = spans.find { it.name == "async-child" }!!
        assertEquals(root.traceId, child.traceId)
        assertEquals(root.spanId, child.parentSpanId)
    }

    @Test
    fun `attributes are set on spans`() = runTest {
        // given
        // - attributes to set on span creation
        val telemetry = createTelemetry()

        // when
        // - start a span with attribute map
        telemetry.inSpan("operation", attributes = mapOf("user.id" to 123, "success" to true)) { _ ->
            // - body intentionally empty
        }

        // then
        // - typed attributes are present on the exported span
        val span = telemetry.inMemorySpans().find { it.name == "operation" }!!
        assertEquals(123L, span.attributes.get(AttributeKey.longKey("user.id")))
        assertEquals(true, span.attributes.get(AttributeKey.booleanKey("success")))
    }
}
