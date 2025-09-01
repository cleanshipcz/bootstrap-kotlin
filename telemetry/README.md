# Telemetry Module

A framework-agnostic telemetry facade for Kotlin apps. It hides vendor SDKs (OpenTelemetry, Micrometer, SLF4J/Logback) behind a simple API and supports configuration-driven exporters for Prometheus (metrics), OTLP (Tempo traces, OTEL metrics), and JSON logging (ready for Loki ingestion). Works in plain Kotlin, Ktor, or Spring Boot.

## Features
- Tracing via OpenTelemetry with W3C Trace Context propagation.
- Metrics via Micrometer: counters, timers, gauges. Prometheus-compatible naming/labels.
- Structured logging via SLF4J/Logback JSON with automatic `trace_id` and `span_id` (no manual MDC).
- Configurable exporters via env vars / system properties.

## Gradle
This module is already included in settings. Add dependency from your application module:
```kotlin
dependencies {
    implementation(project(":telemetry"))
}
```

## Quick start
```kotlin
import cz.cleanship.telemetry.*

fun main() {
    val telemetry = Telemetry.create(TelemetryConfig.fromEnvironment())
    val log = telemetry.logger("example.app")

    val requests = telemetry.counter("http_requests_total", mapOf("method" to "GET"))

    telemetry.inSpan("handle-request", SpanKind.SERVER, mapOf("route" to "/hello")) { span ->
        requests.increment()
        val timer = telemetry.timer("work_duration_ms")
        timer.recordSuspend {
            // do work
        }
        telemetry.gauge("queue_depth").set(3.0)
        log.info("Handled request", mapOf("user" to 42))
    }
}
```

## Configuration (env or -D system properties)
- TELEMETRY_SERVICE_NAME / -Dtelemetry.service.name (default: `bootstrap-kotlin`)
- TELEMETRY_EXPORTER_TRACES / -Dtelemetry.traces.exporter: `none` | `logging` | `otlp` | `inmemory`
- TELEMETRY_EXPORTER_METRICS / -Dtelemetry.metrics.exporter: `none` | `prometheus` | `otlp` | `logging`
- TELEMETRY_OTLP_ENDPOINT / -Dtelemetry.otlp.endpoint (default: none; example: `http://localhost:4318`)

Examples:
```bash
# Log spans to stdout, metrics to Prometheus registry (expose via HTTP yourself)
export TELEMETRY_EXPORTER_TRACES=logging
export TELEMETRY_EXPORTER_METRICS=prometheus

# OTLP to Grafana Tempo/OTel Collector
export TELEMETRY_EXPORTER_TRACES=otlp
export TELEMETRY_OTLP_ENDPOINT=http://otel-collector:4318
```

## Exposing Prometheus metrics
- Plain Kotlin/Ktor: add an endpoint and return `telemetry.prometheusScrape()` as text/plain.
```kotlin
val appTelemetry = Telemetry.create(TelemetryConfig.fromEnvironment())
fun metricsHandler(): String = appTelemetry.prometheusScrape() ?: "prometheus exporter disabled"
```
- Spring Boot: create a simple controller mapping `/metrics` and return the scrape string.

## Structured JSON logging (Logback)
Add a `logback.xml` (or `logback-test.xml`) with Logstash JSON encoder and the provided `TraceJsonProvider`:
```xml
<configuration>
  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
      <providers>
        <timestamp/>
        <loggerName/>
        <threadName/>
        <logLevel/>
        <message/>
        <mdc/>
        <provider class="cz.cleanship.telemetry.logging.TraceJsonProvider"/>
      </providers>
    </encoder>
  </appender>
  <root level="INFO">
    <appender-ref ref="STDOUT"/>
  </root>
</configuration>
```
This will include `trace_id` and `span_id` fields for logs emitted inside spans.

## Testing
Run tests (module only):
```bash
./gradlew :telemetry:test
```
The suite uses:
- InMemorySpanExporter for spans
- Micrometer registry introspection
- Logback ListAppender for log assertions

## Notes
- The facade (`TelemetryFacade`) shields application code from vendor APIs.
- Context propagation across coroutines is handled via OpenTelemetry `Context.asContextElement()`.
- Use `Telemetry.create()` to instantiate the default implementation.
