package cz.cleanship.remento.service

import cz.cleanship.remento.common.dto.CreateTopicRequest
import cz.cleanship.remento.common.dto.TopicDto
import cz.cleanship.remento.common.dto.UpdateTopicRequest
import cz.cleanship.remento.domain.Topic
import cz.cleanship.remento.mapper.toDto
import cz.cleanship.remento.repository.SubjectRepository
import cz.cleanship.remento.repository.TopicRepository
import cz.cleanship.telemetry.TelemetryFacade
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class TopicService(
    private val topicRepository: TopicRepository,
    private val subjectRepository: SubjectRepository,
    telemetry: TelemetryFacade,
) : BaseCrudService<Topic, Long, CreateTopicRequest, UpdateTopicRequest, TopicDto>(
    "Topic", topicRepository, telemetry
) {

    override fun createEntity(req: CreateTopicRequest): Topic {
        val subjectId = req.subjectId ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject ID required")
        val subject = subjectRepository.findById(subjectId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found") }
        return Topic(name = req.name, studyPassage = req.studyPassage, subject = subject)
    }

    override fun updateEntity(entity: Topic, req: UpdateTopicRequest) {
        entity.name = req.name
        entity.studyPassage = req.studyPassage
    }

    override fun toDto(entity: Topic): TopicDto = entity.toDto()

    fun getTopics(subjectId: Long): List<TopicDto> = telemetry.runInSpan("getTopicsBySubject") {
        telemetry.counter("service.getTopicsBySubject").increment()
        topicRepository.findBySubject_Id(subjectId).map { toDto(it) }
    }
}
