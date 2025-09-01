package cz.cleanship.telemetry

/**
 * Configuration for the telemetry module. Values can be constructed from environment variables
 * and/or system properties using [fromEnvironment].
 */
data class TelemetryConfig(
    val serviceName: String = System.getProperty("telemetry.service.name")
        ?: System.getenv("TELEMETRY_SERVICE_NAME")
        ?: "bootstrap-kotlin",
    val tracesExporter: TracesExporter = TracesExporter.from(
        System.getProperty("telemetry.traces.exporter") ?: System.getenv("TELEMETRY_EXPORTER_TRACES")
    ),
    val metricsExporter: MetricsExporter = MetricsExporter.from(
        System.getProperty("telemetry.metrics.exporter") ?: System.getenv("TELEMETRY_EXPORTER_METRICS")
    ),
    val otlpEndpoint: String? = System.getProperty("telemetry.otlp.endpoint")
        ?: System.getenv("TELEMETRY_OTLP_ENDPOINT"),
) {
    companion object {
        fun fromEnvironment(): TelemetryConfig = TelemetryConfig()
    }
}

enum class TracesExporter(val id: String) {
    NONE("none"),
    LOGGING("logging"),
    OTLP("otlp"),
    INMEMORY_FOR_TESTS("inmemory");

    companion object {
        fun from(value: String?): TracesExporter = when (value?.lowercase()) {
            LOGGING.id -> LOGGING
            OTLP.id -> OTLP
            INMEMORY_FOR_TESTS.id -> INMEMORY_FOR_TESTS
            else -> NONE
        }
    }
}

enum class MetricsExporter(val id: String) {
    NONE("none"),
    PROMETHEUS("prometheus"),
    OTLP("otlp"),
    LOGGING("logging");

    companion object {
        fun from(value: String?): MetricsExporter = when (value?.lowercase()) {
            PROMETHEUS.id -> PROMETHEUS
            OTLP.id -> OTLP
            LOGGING.id -> LOGGING
            else -> NONE
        }
    }
}
