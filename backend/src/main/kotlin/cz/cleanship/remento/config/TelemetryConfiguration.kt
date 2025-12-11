package cz.cleanship.remento.config

import cz.cleanship.remento.telemetry.SpringTelemetry
import cz.cleanship.telemetry.TelemetryFacade
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.tracing.Tracer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class TelemetryConfiguration {

    @Bean
    open fun telemetryFacade(
        meterRegistry: MeterRegistry,
        tracer: Tracer
    ): TelemetryFacade {
        return SpringTelemetry(meterRegistry, tracer)
    }
}
