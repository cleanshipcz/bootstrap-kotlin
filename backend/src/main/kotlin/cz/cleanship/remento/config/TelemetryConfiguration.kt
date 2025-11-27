package cz.cleanship.remento.config

import cz.cleanship.telemetry.Telemetry
import cz.cleanship.telemetry.TelemetryConfig
import cz.cleanship.telemetry.TelemetryFacade
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class TelemetryConfiguration(
    @param:Value("\${spring.application.name:remento-backend}") private val serviceName: String,
) {

    @Bean(destroyMethod = "shutdown")
    open fun telemetryFacade(): TelemetryFacade {
        val baseConfig = TelemetryConfig.fromEnvironment()
        val config = baseConfig.copy(serviceName = serviceName)
        return Telemetry.create(config)
    }
}

