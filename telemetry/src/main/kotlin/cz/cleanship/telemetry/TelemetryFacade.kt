package cz.cleanship.telemetry


/**
 * Framework-agnostic facade over tracing, metrics, and logging.
 * Application code should depend only on this interface, not on vendor SDKs.
 */
interface TelemetryFacade {
    // ---- Metrics ----
    fun counter(name: String, tags: Map<String, String> = emptyMap()): CounterHandle
    fun timer(name: String, tags: Map<String, String> = emptyMap()): TimerHandle
    fun gauge(name: String, tags: Map<String, String> = emptyMap()): GaugeHandle

    // ---- Tracing ----
    fun startSpan(name: String, kind: SpanKind = SpanKind.INTERNAL, attributes: Map<String, Any?> = emptyMap()): SpanScope

    suspend fun <T> inSpan(
        name: String,
        kind: SpanKind = SpanKind.INTERNAL,
        attributes: Map<String, Any?> = emptyMap(),
        block: suspend (TelemetrySpan) -> T
    ): T

    // ---- Logging ----
    fun logger(name: String): TelemetryLogger

    // ---- Exporters / Utilities ----
    /** Returns Prometheus scrape text if Prometheus registry is enabled, otherwise null. */
    fun prometheusScrape(): String?

    /** Shutdown exporters/SDKs gracefully. */
    fun shutdown()
}

// ------------ Handles ------------
interface CounterHandle {
    fun increment(amount: Double = 1.0)
}

interface TimerHandle {
    fun record(durationMillis: Long)
    suspend fun <T> recordSuspend(block: suspend () -> T): T
}

interface GaugeHandle {
    fun set(value: Double)
}

/** Holds an active span bound to the current context. Close to end. */
interface SpanScope : AutoCloseable {
    val span: TelemetrySpan
    override fun close()
}

/** Simple structured logger that injects trace_id/span_id automatically. */
interface TelemetryLogger {
    fun info(message: String, fields: Map<String, Any?> = emptyMap())
    fun warn(message: String, fields: Map<String, Any?> = emptyMap())
    fun error(message: String, throwable: Throwable? = null, fields: Map<String, Any?> = emptyMap())
    fun debug(message: String, fields: Map<String, Any?> = emptyMap())
}

/**
 * Minimal span abstraction to avoid leaking vendor types.
 */
interface TelemetrySpan {
    val traceId: String
    val spanId: String
}

enum class SpanKind { INTERNAL, SERVER, CLIENT, PRODUCER, CONSUMER }
