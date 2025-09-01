package cz.cleanship.telemetry

object Telemetry {
    fun create(config: TelemetryConfig = TelemetryConfig.fromEnvironment()): TelemetryFacade = DefaultTelemetry(config)
}
