package cz.cleanship.remento.service

import cz.cleanship.telemetry.SpanKind
import cz.cleanship.telemetry.TelemetryFacade
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

abstract class BaseCrudService<E : Any, ID : Any, CreateReq, UpdateReq, D>(
    private val entityName: String,
    private val repo: JpaRepository<E, ID>,
    protected val telemetry: TelemetryFacade
) {

    abstract fun createEntity(req: CreateReq): E

    abstract fun updateEntity(entity: E, req: UpdateReq)

    abstract fun toDto(entity: E): D

    open fun getAll(): List<D> = telemetry.runInSpan("getAll$entityName") {
        telemetry.counter("service.getAll", mapOf("entity" to entityName)).increment()
        repo.findAll().map { toDto(it) }
    }

    open fun getOne(id: ID): D = telemetry.runInSpan("getOne$entityName") {
        telemetry.counter("service.getOne", mapOf("entity" to entityName)).increment()
        repo.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
            .let { toDto(it) }
    }

    open fun create(req: CreateReq): D = telemetry.runInSpan("create$entityName") {
        telemetry.counter("service.create", mapOf("entity" to entityName)).increment()
        toDto(repo.save(createEntity(req)))
    }

    open fun update(id: ID, req: UpdateReq): D = telemetry.runInSpan("update$entityName") {
        telemetry.counter("service.update", mapOf("entity" to entityName)).increment()
        repo.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
            .also { updateEntity(it, req) }
            .let { toDto(repo.save(it)) }
    }

    open fun delete(id: ID) = telemetry.runInSpan("delete$entityName") {
        telemetry.counter("service.delete", mapOf("entity" to entityName)).increment()
        if (!repo.existsById(id)) throw ResponseStatusException(HttpStatus.NOT_FOUND)
        repo.deleteById(id)
    }

    // Helper to avoid boilerplate
    protected fun <T> TelemetryFacade.runInSpan(name: String, block: () -> T): T {
        // We use runBlocking or similar if inSpan is suspend?
        // TelemetryFacade.inSpan is suspend.
        // But our services are blocking (Spring MVC).
        // We should use startSpan / try-finally for blocking code or change inSpan to be non-suspend.
        // The user's TelemetryFacade has suspend inSpan.
        // For blocking code, we must use startSpan.
        val scope = startSpan(name, SpanKind.INTERNAL)
        return try {
            block()
        } catch (e: Exception) {
            scope.span.recordError(e)
             throw e
        } finally {
            scope.close()
        }
    }

    private fun cz.cleanship.telemetry.TelemetrySpan.recordError(e: Exception) {
        // Not exposed in TelemetrySpan interface provided? 
        // TelemetrySpan interface in TelemetryFacade only has traceId, spanId.
        // I cannot record error on it unless I cast or TelemetryFacade adds method.
        // But I can assume OTel span is active and if I throw, Spring/Micrometer might pick it up if wrapped.
        // However, explicit recording is better.
        // Since I cannot change TelemetrySpan interface easily (it's in another module), I will just let the exception bubble up.
        // Micrometer Tracing should catch it if I used `tracer.startScopedSpan` etc.
        // But I am using `TelemetryFacade`.
    }
}

