package cz.cleanship.remento.service

import cz.cleanship.remento.common.dto.CreateFlashcardRequest
import cz.cleanship.remento.common.dto.FlashcardDto
import cz.cleanship.remento.domain.Flashcard
import cz.cleanship.remento.exception.DuplicateFlashcardQuestionException
import cz.cleanship.remento.mapper.toDto
import cz.cleanship.remento.repository.FlashcardRepository
import cz.cleanship.remento.repository.TopicRepository
import cz.cleanship.telemetry.SpanKind
import cz.cleanship.telemetry.TelemetryFacade
import cz.cleanship.telemetry.TelemetrySpan
import cz.cleanship.telemetry.TimerHandle
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import kotlin.system.measureTimeMillis

interface IFlashcardService {
    fun getFlashcards(topicId: Long): List<FlashcardDto>
    fun createFlashcard(topicId: Long, request: CreateFlashcardRequest): FlashcardDto
    fun deleteFlashcard(topicId: Long, flashcardId: Long)
}

@Service
@Transactional
open class FlashcardService(
    private val flashcardRepository: FlashcardRepository,
    private val topicRepository: TopicRepository,
    private val telemetry: TelemetryFacade,
) : IFlashcardService {

    private val listCounter = telemetry.counter("flashcards_operations_total", mapOf("operation" to "list"))
    private val createCounter = telemetry.counter("flashcards_operations_total", mapOf("operation" to "create"))
    private val deleteCounter = telemetry.counter("flashcards_operations_total", mapOf("operation" to "delete"))
    private val listTimer = telemetry.timer("flashcards_operation_latency_ms", mapOf("operation" to "list"))
    private val createTimer = telemetry.timer("flashcards_operation_latency_ms", mapOf("operation" to "create"))
    private val deleteTimer = telemetry.timer("flashcards_operation_latency_ms", mapOf("operation" to "delete"))

    companion object {
        private val log = LoggerFactory.getLogger(FlashcardService::class.java)
    }

    override fun getFlashcards(topicId: Long): List<FlashcardDto> {
        ensureTopicExists(topicId)
        return tracedOperation(
            "flashcards.list",
            listTimer,
            mapOf("component" to "FlashcardService", "topicId" to topicId),
        ) { span ->
            flashcardRepository.findByTopicId(topicId).map { it.toDto() }.also {
                listCounter.increment()
                log.info("Fetched {} flashcards for topic {} traceId={} spanId={}", it.size, topicId, span.traceId, span.spanId)
            }
        }
    }

    override fun createFlashcard(topicId: Long, request: CreateFlashcardRequest): FlashcardDto {
        val topic = topicRepository.findById(topicId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Topic $topicId not found") }
        val normalizedQuestion = request.question.trim()
        val normalizedAnswer = request.answer.trim()
        if (normalizedQuestion.isBlank() || normalizedAnswer.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Question and answer must not be blank")
        }
        if (flashcardRepository.existsByTopicIdAndQuestion(topicId, normalizedQuestion)) {
            throw DuplicateFlashcardQuestionException(topicId, normalizedQuestion)
        }

        val flashcard = Flashcard(
            question = normalizedQuestion,
            answer = normalizedAnswer,
            topic = topic,
        )

        return tracedOperation(
            name = "flashcards.create",
            timer = createTimer,
            attributes = mapOf("component" to "FlashcardService", "topicId" to topicId),
        ) { span ->
            val savedFlashcard = flashcardRepository.save(flashcard).toDto()
            createCounter.increment()
            log.info("Created flashcard id={} topic={} traceId={} spanId={}", savedFlashcard.id, topicId, span.traceId, span.spanId)
            savedFlashcard
        }
    }

    override fun deleteFlashcard(topicId: Long, flashcardId: Long) {
        val flashcard = flashcardRepository.findById(flashcardId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Flashcard $flashcardId not found") }

        if (flashcard.topic?.id != topicId) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Flashcard $flashcardId does not belong to topic $topicId")
        }

        tracedOperation(
            name = "flashcards.delete",
            timer = deleteTimer,
            attributes = mapOf("flashcard_id" to flashcardId, "topicId" to topicId, "component" to "FlashcardService"),
        ) { span ->
            flashcardRepository.delete(flashcard)
            deleteCounter.increment()
            log.info("Deleted flashcard {} topic={} traceId={} spanId={}", flashcardId, topicId, span.traceId, span.spanId)
        }
    }

    private inline fun <T> tracedOperation(
        name: String,
        timer: TimerHandle,
        attributes: Map<String, Any?> = emptyMap(),
        crossinline block: (TelemetrySpan) -> T,
    ): T {
        telemetry.startSpan(name, SpanKind.INTERNAL, attributes).use { scope ->
            var result: T? = null
            val duration = measureTimeMillis {
                result = block(scope.span)
            }
            timer.record(duration)
            @Suppress("UNCHECKED_CAST")
            return result as T
        }
    }

    private fun ensureTopicExists(topicId: Long) {
        if (!topicRepository.existsById(topicId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Topic $topicId not found")
        }
    }
}
