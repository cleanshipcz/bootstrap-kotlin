package cz.cleanship.remento.telemetry

import cz.cleanship.telemetry.CounterHandle
import cz.cleanship.telemetry.GaugeHandle
import cz.cleanship.telemetry.SpanKind
import cz.cleanship.telemetry.SpanScope
import cz.cleanship.telemetry.TelemetryFacade
import cz.cleanship.telemetry.TelemetrySpan
import cz.cleanship.telemetry.TimerHandle
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Timer
import io.micrometer.tracing.Tracer
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class SpringTelemetry(
    private val meterRegistry: MeterRegistry,
    private val tracer: Tracer
) : TelemetryFacade {

    override fun counter(name: String, tags: Map<String, String>): CounterHandle {
        val counter = meterRegistry.counter(name, tags.map { Tag.of(it.key, it.value) })
        return object : CounterHandle {
            override fun increment(amount: Double) {
                counter.increment(amount)
            }
        }
    }

    override fun timer(name: String, tags: Map<String, String>): TimerHandle {
        val timer = Timer.builder(name)
            .tags(tags.map { Tag.of(it.key, it.value) })
            .register(meterRegistry)
        
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
        val ref = AtomicReference(0.0)
        io.micrometer.core.instrument.Gauge.builder(name, ref) { it.get() }
            .tags(tags.map { Tag.of(it.key, it.value) })
            .register(meterRegistry)
            
        return object : GaugeHandle {
            override fun set(value: Double) {
                ref.set(value)
            }
        }
    }

    override fun startSpan(name: String, kind: SpanKind, attributes: Map<String, Any?>): SpanScope {
        val spanBuilder = tracer.nextSpan().name(name)
        
        // Using tags as fallback for kind if direct method unresolved in this environment
        when (kind) {
            SpanKind.INTERNAL -> spanBuilder.tag("span.kind", "internal")
            SpanKind.SERVER -> spanBuilder.tag("span.kind", "server")
            SpanKind.CLIENT -> spanBuilder.tag("span.kind", "client")
            SpanKind.PRODUCER -> spanBuilder.tag("span.kind", "producer")
            SpanKind.CONSUMER -> spanBuilder.tag("span.kind", "consumer")
        }

        attributes.forEach { (k, v) -> spanBuilder.tag(k, v.toString()) }

        val span = spanBuilder.start()
        val scope = tracer.withSpan(span)

        return object : SpanScope {
            override val span: TelemetrySpan = SpringTelemetrySpan(span)
            
            override fun close() {
                scope.close()
                span.end()
            }
        }
    }

    override suspend fun <T> inSpan(
        name: String,
        kind: SpanKind,
        attributes: Map<String, Any?>,
        block: suspend (TelemetrySpan) -> T
    ): T {
        val spanBuilder = tracer.nextSpan().name(name)
        
         when (kind) {
            SpanKind.INTERNAL -> spanBuilder.tag("span.kind", "internal")
            SpanKind.SERVER -> spanBuilder.tag("span.kind", "server")
            SpanKind.CLIENT -> spanBuilder.tag("span.kind", "client")
            SpanKind.PRODUCER -> spanBuilder.tag("span.kind", "producer")
            SpanKind.CONSUMER -> spanBuilder.tag("span.kind", "consumer")
        }
        attributes.forEach { (k, v) -> spanBuilder.tag(k, v.toString()) }
        
        val span = spanBuilder.start()
        
        try {
            return tracer.withSpan(span).use {
                 block(SpringTelemetrySpan(span))
            }
        } catch (e: Exception) {
            span.error(e)
            throw e
        } finally {
            span.end()
        }
    }

    override fun prometheusScrape(): String? = null

    override fun shutdown() {}
}

private class SpringTelemetrySpan(private val span: io.micrometer.tracing.Span) : TelemetrySpan {
    override val traceId: String get() = span.context().traceId()
    override val spanId: String get() = span.context().spanId()
}
