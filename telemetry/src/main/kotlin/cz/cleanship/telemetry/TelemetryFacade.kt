package cz.cleanship.telemetry

/**
 * Framework-agnostic facade over tracing and metrics.
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
    fun counter(
        name: String,
        tags: Map<String, String> = emptyMap(),
    ): CounterHandle

    /**
     * Returns a timer.
     *
     * @param name metric name
     * @param tags metric tags
     * @return a handle to record durations
     */
    fun timer(
        name: String,
        tags: Map<String, String> = emptyMap(),
    ): TimerHandle

    /**
     * Returns a gauge.
     *
     * @param name metric name
     * @param tags metric tags
     * @return a handle to set gauge values
     */
    fun gauge(
        name: String,
        tags: Map<String, String> = emptyMap(),
    ): GaugeHandle

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
    fun startSpan(
        name: String,
        kind: SpanKind = SpanKind.INTERNAL,
        attributes: Map<String, Any?> = emptyMap(),
    ): SpanScope

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
        block: suspend (TelemetrySpan) -> T,
    ): T

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
