package cz.cleanship.telemetry.support

import io.opentelemetry.api.trace.SpanContext
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.TraceFlags
import io.opentelemetry.api.trace.TraceState
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.testing.trace.TestSpanData
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.data.StatusData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LocalInMemorySpanExporterTest {

    @Test
    fun `export stores spans and reset clears them`() {
        val exporter = LocalInMemorySpanExporter()
        val empty: MutableCollection<SpanData> = mutableListOf()
        assertTrue(exporter.export(empty) == CompletableResultCode.ofSuccess())
        assertEquals(0, exporter.finishedSpanItems.size)

        // Build a minimal valid finished span
        val span = TestSpanData
            .builder()
            .setName("test")
            .setKind(SpanKind.INTERNAL)
            .setSpanContext(
                SpanContext.create(
                    "0123456789abcdef0123456789abcdef",
                    "0123456789abcdef",
                    TraceFlags.getSampled(),
                    TraceState.getDefault(),
                ),
            ).setParentSpanContext(SpanContext.getInvalid())
            .setStartEpochNanos(1L)
            .setEndEpochNanos(2L)
            .setStatus(StatusData.unset())
            .setHasEnded(true)
            .build()

        val batch: MutableCollection<SpanData> = mutableListOf(span)
        assertTrue(exporter.export(batch) == CompletableResultCode.ofSuccess())
        assertEquals(1, exporter.finishedSpanItems.size)
        assertEquals("test", exporter.finishedSpanItems.first().name)

        // reset clears storage
        exporter.reset()
        assertEquals(0, exporter.finishedSpanItems.size)
    }
}
