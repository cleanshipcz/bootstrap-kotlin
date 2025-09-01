package cz.cleanship.telemetry


/**
 * Framework-agnostic facade over tracing, metrics, and logging.
 * Application code should depend only on this interface, not on vendor SDKs.
 */
interface TelemetryFacade {
    // ---- Metrics ----
    /**
     * Returns a counter.
     *
     * @param name metric name
     * @param tags metric tags
     * @return a handle to increment the counter
     */
    fun counter(name: String, tags: Map<String, String> = emptyMap()): CounterHandle

    /**
     * Returns a timer.
     *
     * @param name metric name
     * @param tags metric tags
     * @return a handle to record durations
     */
    fun timer(name: String, tags: Map<String, String> = emptyMap()): TimerHandle

    /**
     * Returns a gauge.
     *
     * @param name metric name
     * @param tags metric tags
     * @return a handle to set gauge values
     */
    fun gauge(name: String, tags: Map<String, String> = emptyMap()): GaugeHandle

    // ---- Tracing ----
    /**
     * Starts a span and makes it current.
     *
     * @param name span name
     * @param kind span kind
     * @param attributes attributes to set on the span
     * @return a scope representing the active span
     * @see inSpan
     */
    fun startSpan(name: String, kind: SpanKind = SpanKind.INTERNAL, attributes: Map<String, Any?> = emptyMap()): SpanScope

    /**
     * Runs [block] within a new span and returns its result.
     *
     * @param name span name
     * @param kind span kind
     * @param attributes attributes to set on the span
     * @param block suspending work executed within the span; receives the created span
     * @return the result of [block]
     */
    suspend fun <T> inSpan(
        name: String,
        kind: SpanKind = SpanKind.INTERNAL,
        attributes: Map<String, Any?> = emptyMap(),
        block: suspend (TelemetrySpan) -> T
    ): T

    // ---- Logging ----
    /**
     * Returns a structured logger bound to the given name.
     *
     * The returned logger adds the current trace and span IDs (when present) to MDC for each call
     * and supports per-call structured fields.
     *
     * @param name the SLF4J logger name
     * @return a logger that enriches MDC with tracing identifiers
     * @see TelemetryLogger
     */
    fun logger(name: String): TelemetryLogger

    // ---- Exporters / Utilities ----
    /**
     * Returns Prometheus exposition text.
     *
     * @return scrape text if Prometheus is enabled; otherwise null
     */
    fun prometheusScrape(): String?

    /** Shutdown exporters/SDKs gracefully. */
    fun shutdown()
}

// ------------ Handles ------------
interface CounterHandle {
    /**
     * Increments the counter by [amount].
     *
     * @param amount increment amount (default 1.0)
     */
    fun increment(amount: Double = 1.0)
}

interface TimerHandle {
    /**
     * Records a duration.
     *
     * @param durationMillis duration in milliseconds
     */
    fun record(durationMillis: Long)

    /**
     * Measures the time to execute [block] and records it.
     *
     * @param block suspending work to time
     * @return the result of [block]
     */
    suspend fun <T> recordSuspend(block: suspend () -> T): T
}

interface GaugeHandle {
    /**
     * Sets the current gauge value.
     *
     * @param value value to report
     */
    fun set(value: Double)
}

/** Holds an active span bound to the current context. */
interface SpanScope : AutoCloseable {
    /** The active span. */
    val span: TelemetrySpan
    /** Ends the span and restores the previous context. */
    override fun close()
}

/**
 * Structured logger that enriches SLF4J MDC with trace and span IDs when available.
 *
 * All MDC entries added by a logging call are removed afterwards.
 *
 * @see cz.cleanship.telemetry.logging.TraceJsonProvider
 */
interface TelemetryLogger {
    /**
     * Logs at INFO level.
     *
     * @param message the log message
     * @param fields structured fields added to MDC for this call
     */
    fun info(message: String, fields: Map<String, Any?> = emptyMap())

    /**
     * Logs at WARN level.
     *
     * @param message the log message
     * @param fields structured fields added to MDC for this call
     */
    fun warn(message: String, fields: Map<String, Any?> = emptyMap())

    /**
     * Logs at ERROR level.
     *
     * @param message the log message
     * @param throwable optional error to log
     * @param fields structured fields added to MDC for this call
     */
    fun error(message: String, throwable: Throwable? = null, fields: Map<String, Any?> = emptyMap())

    /**
     * Logs at DEBUG level.
     *
     * @param message the log message
     * @param fields structured fields added to MDC for this call
     */
    fun debug(message: String, fields: Map<String, Any?> = emptyMap())
}

/**
 * Minimal span abstraction to avoid leaking vendor types.
 */
interface TelemetrySpan {
    /**
     * The trace ID.
     */
    val traceId: String
    /**
     * The span ID.
     */
    val spanId: String
}

/**
 * Span kind for new spans.
 */
enum class SpanKind { INTERNAL, SERVER, CLIENT, PRODUCER, CONSUMER }
