package cz.cleanship.remento.service

import cz.cleanship.remento.common.dto.CreateFlashcardRequest
import cz.cleanship.remento.domain.Flashcard
import cz.cleanship.remento.domain.Subject
import cz.cleanship.remento.domain.Topic
import cz.cleanship.remento.repository.FlashcardRepository
import cz.cleanship.remento.repository.TopicRepository
import cz.cleanship.remento.common.dto.UpdateFlashcardRequest
import cz.cleanship.remento.testsupport.NoOpTelemetry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.web.server.ResponseStatusException

class FlashcardServiceTest {

    private val flashcardRepository: FlashcardRepository = mockk()
    private val topicRepository: TopicRepository = mockk()
    // No mapper injection needed
    private val flashcardService = FlashcardService(flashcardRepository, topicRepository, NoOpTelemetry())

    private val subject = Subject(id = 1L, name = "Languages")
    private val topic = Topic(id = 2L, name = "Kotlin", studyPassage = "Basics", subject = subject)

    @Test
    fun `getFlashcards should return all flashcards converted to DTOs`() {
        val flashcard1 = Flashcard(id = 1L, question = "Q1", answer = "A1", topic = topic, subject = subject)
        val flashcard2 = Flashcard(id = 2L, question = "Q2", answer = "A2", topic = topic, subject = subject)
        every { flashcardRepository.findByTopicId(topic.id!!) } returns listOf(flashcard1, flashcard2)

        val result = flashcardService.getFlashcards(topic.id!!)

        assertThat(result).hasSize(2)
        assertThat(result[0].question).isEqualTo("Q1")
        assertThat(result[0].topicId).isEqualTo(topic.id)
        verify(exactly = 1) { flashcardRepository.findByTopicId(topic.id!!) }
    }

    @Test
    fun `create should save flashcard and return DTO`() {
        val request = CreateFlashcardRequest(topicId = topic.id!!, question = "Q", answer = "A")
        val savedFlashcard = Flashcard(id = 1L, question = "Q", answer = "A", topic = topic, subject = subject)

        every { topicRepository.findById(topic.id!!) } returns java.util.Optional.of(topic)
        every { flashcardRepository.save(any()) } returns savedFlashcard

        val result = flashcardService.create(request)

        assertThat(result.id).isEqualTo(1L)
        assertThat(result.subjectId).isEqualTo(subject.id)
        assertThat(result.question).isEqualTo("Q")
        assertThat(result.answer).isEqualTo("A")
        assertThat(result.topicId).isEqualTo(topic.id)

        verify(exactly = 1) {
            flashcardRepository.save(
                withArg {
                    assertThat(it.question).isEqualTo("Q")
                    assertThat(it.answer).isEqualTo("A")
                    assertThat(it.topic).isEqualTo(topic)
                },
            )
        }
    }

    @Test
    fun `delete should delete flashcard`() {
        val flashcard = Flashcard(id = 1L, question = "Q", answer = "A", topic = topic, subject = subject)
        every { flashcardRepository.existsById(1L) } returns true
        every { flashcardRepository.deleteById(1L) } returns Unit

        flashcardService.delete(1L)

        verify(exactly = 1) { flashcardRepository.deleteById(1L) }
    }

    @Test
    fun `update should persist changes`() {
        val flashcard = Flashcard(id = 10L, question = "Old", answer = "Answer", topic = topic, subject = subject)
        val request = UpdateFlashcardRequest(question = "New Question", answer = "New Answer")
        every { flashcardRepository.findById(10L) } returns java.util.Optional.of(flashcard)
        every { flashcardRepository.save(flashcard) } returns flashcard

        val result = flashcardService.update(10L, request)

        assertThat(result.question).isEqualTo("New Question")
        assertThat(result.answer).isEqualTo("New Answer")
        verify(exactly = 1) { flashcardRepository.save(flashcard) }
    }

}
