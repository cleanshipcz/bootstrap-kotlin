package cz.cleanship.telemetry.support

import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SpanExporter
import java.util.Collections

/**
 * Lightweight in-memory span exporter for tests. Not thread-safe for high concurrency scenarios,
 * but sufficient for unit tests.
 */
class LocalInMemorySpanExporter : SpanExporter {
    private val _spans: MutableList<SpanData> = Collections.synchronizedList(mutableListOf())
    val finishedSpanItems: List<SpanData> get() = ArrayList(_spans)

    override fun export(spans: MutableCollection<SpanData>): CompletableResultCode {
        _spans.addAll(spans)
        return CompletableResultCode.ofSuccess()
    }

    override fun flush(): CompletableResultCode = CompletableResultCode.ofSuccess()

    override fun shutdown(): CompletableResultCode = CompletableResultCode.ofSuccess()

    fun reset() { _spans.clear() }
}
