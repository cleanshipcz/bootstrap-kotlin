package cz.cleanship.remento.service

import cz.cleanship.remento.common.dto.CreateFlashcardRequest
import cz.cleanship.remento.common.dto.FlashcardDto
import cz.cleanship.remento.common.dto.UpdateFlashcardRequest
import cz.cleanship.remento.domain.Flashcard
import cz.cleanship.remento.exception.DuplicateFlashcardQuestionException
import cz.cleanship.remento.mapper.toDto
import cz.cleanship.remento.repository.FlashcardRepository
import cz.cleanship.remento.repository.TopicRepository
import cz.cleanship.telemetry.TelemetryFacade
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class FlashcardService(
    private val flashcardRepository: FlashcardRepository,
    private val topicRepository: TopicRepository,
    telemetry: TelemetryFacade,
) : BaseCrudService<Flashcard, Long, CreateFlashcardRequest, UpdateFlashcardRequest, FlashcardDto>(
    "Flashcard", flashcardRepository, telemetry
) {

    override fun createEntity(req: CreateFlashcardRequest): Flashcard {
        val topicId = req.topicId ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Topic ID required")
        
        val topic = topicRepository.findById(topicId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found") }

        return Flashcard(
            question = req.question,
            answer = req.answer,
            topic = topic,
            subject = topic.subject
        )
    }

    override fun updateEntity(entity: Flashcard, req: UpdateFlashcardRequest) {
        val topicId = entity.topic?.id ?: throw IllegalStateException("Flashcard topic is null")
        
        entity.question = req.question
        entity.answer = req.answer
    }

    override fun toDto(entity: Flashcard): FlashcardDto = entity.toDto()

    fun getFlashcards(topicId: Long): List<FlashcardDto> = telemetry.runInSpan("getFlashcardsByTopic") {
        telemetry.counter("service.getFlashcardsByTopic").increment()
        flashcardRepository.findByTopicId(topicId).map { toDto(it) }
    }
}
