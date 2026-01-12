package cz.cleanship.remento.testsupport

import cz.cleanship.telemetry.CounterHandle
import cz.cleanship.telemetry.GaugeHandle
import cz.cleanship.telemetry.SpanKind
import cz.cleanship.telemetry.SpanScope
import cz.cleanship.telemetry.TelemetryFacade
import cz.cleanship.telemetry.TelemetrySpan
import cz.cleanship.telemetry.TimerHandle

class NoOpTelemetry : TelemetryFacade {
    private val span = object : TelemetrySpan {
        override val traceId: String = "trace"
        override val spanId: String = "span"
    }

    private val counterHandle = object : CounterHandle {
        override fun increment(amount: Double) {}
    }

    private val timerHandle = object : TimerHandle {
        override fun record(durationMillis: Long) {}
        override suspend fun <T> recordSuspend(block: suspend () -> T): T = block()
    }

    private val gaugeHandle = GaugeHandle { _ -> }

    override fun counter(name: String, tags: Map<String, String>): CounterHandle = counterHandle

    override fun timer(name: String, tags: Map<String, String>): TimerHandle = timerHandle

    override fun gauge(name: String, tags: Map<String, String>): GaugeHandle = gaugeHandle

    override fun startSpan(name: String, kind: SpanKind, attributes: Map<String, Any?>): SpanScope =
        object : SpanScope {
            override val span: TelemetrySpan
                get() = this@NoOpTelemetry.span

            override fun close() {}
        }

    override suspend fun <T> inSpan(
        name: String,
        kind: SpanKind,
        attributes: Map<String, Any?>,
        block: suspend (TelemetrySpan) -> T,
    ): T = block(span)

    override fun prometheusScrape(): String? = null

    override fun shutdown() {}
}


