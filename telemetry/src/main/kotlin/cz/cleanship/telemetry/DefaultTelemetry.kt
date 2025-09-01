package cz.cleanship.telemetry

import cz.cleanship.telemetry.support.LocalInMemorySpanExporter
import io.micrometer.core.instrument.*
import io.micrometer.core.instrument.composite.CompositeMeterRegistry
import io.micrometer.core.instrument.logging.LoggingMeterRegistry
import io.micrometer.core.instrument.logging.LoggingRegistryConfig
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.micrometer.registry.otlp.OtlpConfig
import io.micrometer.registry.otlp.OtlpMeterRegistry
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.exporter.logging.LoggingSpanExporter
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.extension.kotlin.asContextElement
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.sdk.trace.export.SpanExporter
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Default implementation of [TelemetryFacade] using OpenTelemetry and Micrometer.
 *
 * @param config telemetry configuration
 * @param testSpanExporter optional span exporter used for tests
 */
class DefaultTelemetry(
    private val config: TelemetryConfig = TelemetryConfig.fromEnvironment(),
    private val testSpanExporter: SpanExporter? = null,
) : TelemetryFacade {

    private val meterRegistry: CompositeMeterRegistry = CompositeMeterRegistry()
    private var prometheusRegistry: PrometheusMeterRegistry? = null

    private val openTelemetry: OpenTelemetry
    private val tracer: Tracer
    private val sdkTracerProvider: SdkTracerProvider
    private var localInMemory: LocalInMemorySpanExporter? = null

    init {
        // Metrics registries
        val simple = SimpleMeterRegistry()
        meterRegistry.add(simple)
        for (exporter in config.metricsExporters) {
            when (exporter) {
                MetricsExporter.PROMETHEUS -> {
                    val prom = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
                    meterRegistry.add(prom)
                    prometheusRegistry = prom
                }
                MetricsExporter.LOGGING -> {
                    val logging = LoggingMeterRegistry(object : LoggingRegistryConfig {
                        override fun get(key: String) = null
                        override fun step(): Duration = Duration.ofSeconds(10)
                    }, Clock.SYSTEM)
                    meterRegistry.add(logging)
                }
                MetricsExporter.OTLP -> {
                    val endpoint = config.otlpEndpoint ?: "http://localhost:4318"
                    val otlp = OtlpMeterRegistry(
                        object : OtlpConfig {
                            override fun get(key: String): String? = null
                            override fun url(): String = endpoint
                        },
                        Clock.SYSTEM
                    )
                    meterRegistry.add(otlp)
                }
                MetricsExporter.NONE -> { /* skip */ }
            }
        }

        // Tracing
        val spanExporters = mutableListOf<Pair<SpanExporter, Boolean>>() // exporter to isInMemory
        for (exporter in config.tracesExporters) {
            when (exporter) {
                TracesExporter.LOGGING -> spanExporters += LoggingSpanExporter.create() to false
                TracesExporter.OTLP -> {
                    val builder = OtlpGrpcSpanExporter.builder()
                    config.otlpEndpoint?.let { builder.setEndpoint(it) }
                    spanExporters += builder.build() to false
                }
                TracesExporter.INMEMORY_FOR_TESTS -> {
                    val inMem = (testSpanExporter as? LocalInMemorySpanExporter)
                        ?: LocalInMemorySpanExporter()
                    localInMemory = inMem
                    spanExporters += inMem to true
                }
                TracesExporter.NONE -> { /* skip */ }
            }
        }

        val resource = Resource.create(
            io.opentelemetry.api.common.Attributes.of(
                AttributeKey.stringKey("service.name"), config.serviceName
            )
        )

        val tracerProviderBuilder = SdkTracerProvider.builder().setResource(resource)
        for ((exporter, isInMemory) in spanExporters) {
            val processor = if (isInMemory) {
                SimpleSpanProcessor.create(exporter)
            } else {
                BatchSpanProcessor.builder(exporter).build()
            }
            tracerProviderBuilder.addSpanProcessor(processor)
        }
        val tracerProvider = tracerProviderBuilder.build()
        sdkTracerProvider = tracerProvider

        openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setPropagators(ContextPropagators.create(io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator.getInstance()))
            .build()

        tracer = openTelemetry.getTracer("cz.cleanship.telemetry", "1.0.0")
    }

    override fun counter(name: String, tags: Map<String, String>): CounterHandle {
        val metricName = sanitizeMetricName(name)
        val micrometerTags = tagsToMicrometer(tags)
        val counter = meterRegistry.counter(metricName, micrometerTags)
        return object : CounterHandle {
            override fun increment(amount: Double) {
                counter.increment(amount)
            }
        }
    }

    override fun timer(name: String, tags: Map<String, String>): TimerHandle {
        val metricName = sanitizeMetricName(name)
        val micrometerTags = tagsToMicrometer(tags)
        val timer = Timer.builder(metricName).tags(micrometerTags).register(meterRegistry)
        return object : TimerHandle {
            override fun record(durationMillis: Long) {
                timer.record(durationMillis, TimeUnit.MILLISECONDS)
            }

            override suspend fun <T> recordSuspend(block: suspend () -> T): T {
                val start = System.nanoTime()
                try {
                    return block()
                } finally {
                    val end = System.nanoTime()
                    val duration = end - start
                    timer.record(duration, TimeUnit.NANOSECONDS)
                }
            }
        }
    }

    override fun gauge(name: String, tags: Map<String, String>): GaugeHandle {
        val metricName = sanitizeMetricName(name)
        val micrometerTags = tagsToMicrometer(tags)
        val ref = AtomicReference(0.0)
        Gauge.builder(metricName, ref) { r -> r.get() }.tags(micrometerTags).register(meterRegistry)
        return object : GaugeHandle {
            override fun set(value: Double) {
                ref.set(value)
            }
        }
    }

    override fun startSpan(name: String, kind: SpanKind, attributes: Map<String, Any?>): SpanScope {
        val builder = tracer.spanBuilder(name).setSpanKind(kind.toOtel()).setParent(Context.current())
        setSpanAttributes(builder, attributes)
        val otelSpan = builder.startSpan()
        val scope = otelSpan.makeCurrent()
        return object : SpanScope {
            override val span: TelemetrySpan = TelemetrySpanImpl(otelSpan)
            override fun close() {
                scope.close()
                otelSpan.end()
            }
        }
    }

    override suspend fun <T> inSpan(
        name: String,
        kind: SpanKind,
        attributes: Map<String, Any?>,
        block: suspend (TelemetrySpan) -> T
    ): T {
        val parentContext = Context.current()
        val builder = tracer.spanBuilder(name).setSpanKind(kind.toOtel()).setParent(parentContext)
        setSpanAttributes(builder, attributes)
        val otelSpan = builder.startSpan()
        try {
            val scope = otelSpan.makeCurrent()
            try {
                // Propagate the CURRENT OTel context (which includes this span) across coroutines
                return withContext(Context.current().asContextElement()) {
                    try {
                        block(TelemetrySpanImpl(otelSpan))
                    } catch (t: Throwable) {
                        otelSpan.recordException(t)
                        otelSpan.setStatus(StatusCode.ERROR)
                        throw t
                    }
                }
            } finally {
                scope.close()
            }
        } finally {
            otelSpan.end()
        }
    }

    override fun logger(name: String): TelemetryLogger = TelemetryLoggerImpl(name)

    override fun prometheusScrape(): String? = prometheusRegistry?.scrape()

    override fun shutdown() {
        try {
            sdkTracerProvider.shutdown().join(10, TimeUnit.SECONDS)
        } catch (_: Throwable) {
            // ignore
        }
    }

    // ---------- Helpers ----------

    private fun sanitizeMetricName(raw: String): String = raw
        .replace('-', '_')
        .replace('.', '_')
        .replace(Regex("[^a-zA-Z0-9_:]"), "_")

    private fun sanitizeLabelName(raw: String): String = sanitizeMetricName(raw)

    private fun sanitizeLabelValue(raw: String): String = raw
        .replace('"', '_')
        .replace(' ', '_')

    private fun tagsToMicrometer(tags: Map<String, String>): Iterable<Tag> =
        tags.map { Tag.of(sanitizeLabelName(it.key), sanitizeLabelValue(it.value)) }

    private fun setSpanAttributes(builder: io.opentelemetry.api.trace.SpanBuilder, attributes: Map<String, Any?>) {
        for ((k, v) in attributes) {
            when (v) {
                null -> Unit
                is String -> builder.setAttribute(AttributeKey.stringKey(k), v)
                is Boolean -> builder.setAttribute(AttributeKey.booleanKey(k), v)
                is Int -> builder.setAttribute(AttributeKey.longKey(k), v.toLong())
                is Long -> builder.setAttribute(AttributeKey.longKey(k), v)
                is Double -> builder.setAttribute(AttributeKey.doubleKey(k), v)
                is Float -> builder.setAttribute(AttributeKey.doubleKey(k), v.toDouble())
                else -> builder.setAttribute(AttributeKey.stringKey(k), v.toString())
            }
        }
    }

    /**
     * SLF4J-backed logger that adds `trace_id` and `span_id` to MDC for each call.
     *
     * @param name SLF4J logger name
     */
    private class TelemetryLoggerImpl(name: String) : TelemetryLogger {
        private val logger = LoggerFactory.getLogger(name)

        override fun info(message: String, fields: Map<String, Any?>) = withTraceMdc(fields) { logger.info(message) }
        override fun warn(message: String, fields: Map<String, Any?>) = withTraceMdc(fields) { logger.warn(message) }
        override fun error(message: String, throwable: Throwable?, fields: Map<String, Any?>) = withTraceMdc(fields) {
            if (throwable != null) logger.error(message, throwable) else logger.error(message)
        }

        override fun debug(message: String, fields: Map<String, Any?>) = withTraceMdc(fields) { logger.debug(message) }

        /**
         * Executes [logCall] with temporary MDC entries.
         *
         * @param fields structured fields to add for this call
         * @param logCall logging action
         */
        private inline fun withTraceMdc(fields: Map<String, Any?>, crossinline logCall: () -> Unit) {
            val span = io.opentelemetry.api.trace.Span.current()
            val ctx = span.spanContext
            val addedKeys = mutableListOf<String>()
            if (ctx.isValid) {
                putMdc("trace_id", ctx.traceId, addedKeys)
                putMdc("span_id", ctx.spanId, addedKeys)
            }
            for ((k, v) in fields) putMdc(k, v?.toString() ?: "null", addedKeys)
            try { logCall() } finally {
                // Clean up keys we added to MDC to avoid leaking state across threads
                for (k in addedKeys) MDC.remove(k)
            }
        }
        /**
         * Adds a key/value to MDC and tracks it for cleanup.
         *
         * @param key MDC key
         * @param value MDC value
         * @param added collection tracking added keys
         */
        private fun putMdc(key: String, value: String, added: MutableList<String>) {
            MDC.put(key, value); added.add(key)
        }
    }

    // Internal accessors for tests
    internal fun inMemorySpans(): List<SpanData> {
        check(config.tracesExporters.contains(TracesExporter.INMEMORY_FOR_TESTS)) { "InMemory exporter not active" }
        localInMemory?.let { return it.finishedSpanItems }
        val e = testSpanExporter
        if (e is LocalInMemorySpanExporter) return e.finishedSpanItems
        error("InMemory exporter not available")
    }

    internal fun registry(): MeterRegistry = meterRegistry
}

private fun SpanKind.toOtel(): io.opentelemetry.api.trace.SpanKind = when (this) {
    SpanKind.INTERNAL -> io.opentelemetry.api.trace.SpanKind.INTERNAL
    SpanKind.SERVER -> io.opentelemetry.api.trace.SpanKind.SERVER
    SpanKind.CLIENT -> io.opentelemetry.api.trace.SpanKind.CLIENT
    SpanKind.PRODUCER -> io.opentelemetry.api.trace.SpanKind.PRODUCER
    SpanKind.CONSUMER -> io.opentelemetry.api.trace.SpanKind.CONSUMER
}

private class TelemetrySpanImpl(private val delegate: io.opentelemetry.api.trace.Span) : TelemetrySpan {
    private val ctx = delegate.spanContext
    override val traceId: String get() = ctx.traceId
    override val spanId: String get() = ctx.spanId
}
