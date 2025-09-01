package cz.cleanship.telemetry.logging

import com.fasterxml.jackson.core.JsonGenerator
import io.opentelemetry.api.trace.Span
import net.logstash.logback.composite.AbstractJsonProvider
import ch.qos.logback.classic.spi.ILoggingEvent

/**
 * Logstash JSON provider that adds trace_id and span_id fields from the current OpenTelemetry span.
 * This requires logstash-logback-encoder and configuration in logback.xml:
 *
 * <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
 *   <providers>
 *     <provider class="cz.cleanship.telemetry.logging.TraceJsonProvider"/>
 *     <message/>
 *     <loggerName/>
 *     <threadName/>
 *     <mdc/> <!-- optional -->
 *   </providers>
 * </encoder>
 */
class TraceJsonProvider : AbstractJsonProvider<ILoggingEvent>() {
    override fun writeTo(generator: JsonGenerator, event: ILoggingEvent) {
        val ctx = Span.current().spanContext
        if (ctx.isValid) {
            generator.writeStringField("trace_id", ctx.traceId)
            generator.writeStringField("span_id", ctx.spanId)
        }
    }
}
