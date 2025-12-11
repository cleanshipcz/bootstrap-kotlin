package cz.cleanship.remento.service

import cz.cleanship.remento.common.dto.CreateTopicRequest
import cz.cleanship.remento.common.dto.UpdateTopicRequest
import cz.cleanship.remento.domain.Subject
import cz.cleanship.remento.domain.Topic
import cz.cleanship.remento.repository.SubjectRepository
import cz.cleanship.remento.repository.TopicRepository
import cz.cleanship.remento.testsupport.NoOpTelemetry
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.Optional

class TopicServiceTest {

    private val topicRepository: TopicRepository = mockk()
    private val subjectRepository: SubjectRepository = mockk()
    // No mapper needed
    private val topicService = TopicService(topicRepository, subjectRepository, NoOpTelemetry())

    private val subject = Subject(id = 1L, name = "Math")

    @Test
    fun `create should save topic linked to subject`() {
        val request = CreateTopicRequest(subjectId = 1L, name = "Algebra", studyPassage = "Intro")
        val topic = Topic(id = 10L, name = "Algebra", studyPassage = "Intro", subject = subject)

        every { subjectRepository.findById(1L) } returns Optional.of(subject)
        every { topicRepository.save(any()) } returns topic

        val result = topicService.create(request)

        assertThat(result.name).isEqualTo("Algebra")
        assertThat(result.subjectId).isEqualTo(1L)
    }

    @Test
    fun `getTopics should return topics for subject`() {
        val topic = Topic(id = 10L, name = "Algebra", studyPassage = "Intro", subject = subject)
        every { topicRepository.findBySubject_Id(1L) } returns listOf(topic)

        val result = topicService.getTopics(1L)

        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("Algebra")
    }
}
