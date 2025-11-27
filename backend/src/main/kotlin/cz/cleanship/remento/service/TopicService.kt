package cz.cleanship.remento.service

import cz.cleanship.remento.common.dto.CreateTopicRequest
import cz.cleanship.remento.common.dto.TopicDto
import cz.cleanship.remento.common.dto.UpdateTopicRequest
import cz.cleanship.remento.domain.Topic
import cz.cleanship.remento.mapper.toDto
import cz.cleanship.remento.repository.SubjectRepository
import cz.cleanship.remento.repository.TopicRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

interface ITopicService {
    fun getTopics(subjectId: Long): List<TopicDto>
    fun getTopic(topicId: Long): TopicDto
    fun createTopic(subjectId: Long, request: CreateTopicRequest): TopicDto
    fun updateTopic(topicId: Long, request: UpdateTopicRequest): TopicDto
    fun deleteTopic(topicId: Long)
}

@Service
@Transactional
open class TopicService(
    private val topicRepository: TopicRepository,
    private val subjectRepository: SubjectRepository,
) : ITopicService {

    override fun getTopics(subjectId: Long): List<TopicDto> {
        ensureSubjectExists(subjectId)
        return topicRepository.findBySubjectId(subjectId).map { it.toDto(includeFlashcards = false) }
    }

    override fun getTopic(topicId: Long): TopicDto =
        findTopic(topicId).toDto(includeFlashcards = true)

    override fun createTopic(subjectId: Long, request: CreateTopicRequest): TopicDto {
        val subject = subjectRepository.findById(subjectId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Subject $subjectId not found") }

        validateTopic(request.name, request.studyPassage)

        val topic = Topic(
            name = request.name.trim(),
            studyPassage = request.studyPassage.trim(),
            subject = subject,
        )
        return topicRepository.save(topic).toDto(includeFlashcards = true)
    }

    override fun updateTopic(topicId: Long, request: UpdateTopicRequest): TopicDto {
        validateTopic(request.name, request.studyPassage)

        val topic = findTopic(topicId)
        topic.name = request.name.trim()
        topic.studyPassage = request.studyPassage.trim()
        return topicRepository.save(topic).toDto(includeFlashcards = true)
    }

    override fun deleteTopic(topicId: Long) {
        if (!topicRepository.existsById(topicId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Topic $topicId not found")
        }
        topicRepository.deleteById(topicId)
    }

    private fun findTopic(topicId: Long): Topic =
        topicRepository.findById(topicId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Topic $topicId not found") }

    private fun ensureSubjectExists(subjectId: Long) {
        if (!subjectRepository.existsById(subjectId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Subject $subjectId not found")
        }
    }

    private fun validateTopic(name: String, passage: String) {
        if (name.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Topic name must not be blank")
        }
        if (passage.length > 2000) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Study passage must be at most 2000 characters")
        }
    }
}

