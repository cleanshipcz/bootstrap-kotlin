package cz.cleanship.remento.service

import cz.cleanship.remento.common.dto.CreateFlashcardRequest
import cz.cleanship.remento.domain.Flashcard
import cz.cleanship.remento.domain.Subject
import cz.cleanship.remento.domain.Topic
import cz.cleanship.remento.exception.DuplicateFlashcardQuestionException
import cz.cleanship.remento.repository.FlashcardRepository
import cz.cleanship.remento.repository.TopicRepository
import cz.cleanship.remento.common.dto.UpdateFlashcardRequest
import cz.cleanship.telemetry.CounterHandle
import cz.cleanship.telemetry.GaugeHandle
import cz.cleanship.telemetry.SpanKind
import cz.cleanship.telemetry.SpanScope
import cz.cleanship.telemetry.TelemetryFacade
import cz.cleanship.telemetry.TelemetrySpan
import cz.cleanship.telemetry.TimerHandle
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.web.server.ResponseStatusException

class FlashcardServiceTest {

    private val flashcardRepository: FlashcardRepository = mockk()
    private val topicRepository: TopicRepository = mockk()
    private val flashcardService = FlashcardService(flashcardRepository, topicRepository, NoOpTelemetry())

    private val subject = Subject(id = 1L, name = "Languages")
    private val topic = Topic(id = 2L, name = "Kotlin", studyPassage = "Basics", subject = subject)

    @Test
    fun `getFlashcards should return all flashcards converted to DTOs`() {
        val flashcard1 = Flashcard(id = 1L, question = "Q1", answer = "A1", topic = topic)
        val flashcard2 = Flashcard(id = 2L, question = "Q2", answer = "A2", topic = topic)
        every { topicRepository.existsById(topic.id!!) } returns true
        every { flashcardRepository.findByTopicId(topic.id!!) } returns listOf(flashcard1, flashcard2)

        val result = flashcardService.getFlashcards(topic.id!!)

        assertThat(result).hasSize(2)
        assertThat(result[0].question).isEqualTo("Q1")
        assertThat(result[0].topicId).isEqualTo(topic.id)
        verify(exactly = 1) { flashcardRepository.findByTopicId(topic.id!!) }
    }

    @Test
    fun `createFlashcard should save flashcard and return DTO`() {
        val request = CreateFlashcardRequest(question = "Q", answer = "A")
        val savedFlashcard = Flashcard(id = 1L, question = "Q", answer = "A", topic = topic)

        every { topicRepository.findById(topic.id!!) } returns java.util.Optional.of(topic)
        every { flashcardRepository.existsByTopicIdAndQuestion(topic.id!!, "Q") } returns false
        every { flashcardRepository.save(any()) } returns savedFlashcard

        val result = flashcardService.createFlashcard(topic.id!!, request)

        assertThat(result.id).isEqualTo(1L)
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
    fun `createFlashcard should throw DuplicateFlashcardQuestionException for duplicates`() {
        val request = CreateFlashcardRequest(question = "Q", answer = "A")

        every { topicRepository.findById(topic.id!!) } returns java.util.Optional.of(topic)
        every { flashcardRepository.existsByTopicIdAndQuestion(topic.id!!, "Q") } returns true

        assertThatThrownBy { flashcardService.createFlashcard(topic.id!!, request) }
            .isInstanceOf(DuplicateFlashcardQuestionException::class.java)
    }

    @Test
    fun `deleteFlashcard should delete flashcard when it belongs to topic`() {
        val flashcard = Flashcard(id = 1L, question = "Q", answer = "A", topic = topic)
        every { flashcardRepository.findById(1L) } returns java.util.Optional.of(flashcard)
        every { flashcardRepository.delete(flashcard) } returns Unit

        flashcardService.deleteFlashcard(topic.id!!, 1L)

        verify(exactly = 1) { flashcardRepository.delete(flashcard) }
    }

    @Test
    fun `deleteFlashcard should throw when flashcard belongs to different topic`() {
        val otherTopic = Topic(id = 99L, name = "Other", studyPassage = "text", subject = subject)
        val flashcard = Flashcard(id = 1L, question = "Q", answer = "A", topic = otherTopic)
        every { flashcardRepository.findById(1L) } returns java.util.Optional.of(flashcard)

        assertThatThrownBy { flashcardService.deleteFlashcard(topic.id!!, 1L) }
            .isInstanceOf(ResponseStatusException::class.java)
            .hasMessageContaining("does not belong to topic")
    }

    @Test
    fun `updateFlashcard should persist changes`() {
        val flashcard = Flashcard(id = 10L, question = "Old", answer = "Answer", topic = topic)
        val request = UpdateFlashcardRequest(question = "New Question", answer = "New Answer")
        every { flashcardRepository.findById(10L) } returns java.util.Optional.of(flashcard)
        every { flashcardRepository.existsByTopicIdAndQuestionAndIdNot(topic.id!!, "New Question", 10L) } returns false
        every { flashcardRepository.save(flashcard) } returns flashcard

        val result = flashcardService.updateFlashcard(topic.id!!, 10L, request)

        assertThat(result.question).isEqualTo("New Question")
        assertThat(result.answer).isEqualTo("New Answer")
        verify(exactly = 1) { flashcardRepository.save(flashcard) }
    }

    @Test
    fun `updateFlashcard should reject duplicates`() {
        val flashcard = Flashcard(id = 11L, question = "Old", answer = "Answer", topic = topic)
        val request = UpdateFlashcardRequest(question = "Duplicate", answer = "Keep")
        every { flashcardRepository.findById(11L) } returns java.util.Optional.of(flashcard)
        every {
            flashcardRepository.existsByTopicIdAndQuestionAndIdNot(topic.id!!, "Duplicate", 11L)
        } returns true

        assertThatThrownBy { flashcardService.updateFlashcard(topic.id!!, 11L, request) }
            .isInstanceOf(DuplicateFlashcardQuestionException::class.java)
    }

    @Test
    fun `updateFlashcard should verify topic ownership`() {
        val otherTopic = Topic(id = 9L, name = "Other", studyPassage = "text", subject = subject)
        val flashcard = Flashcard(id = 12L, question = "Old", answer = "Answer", topic = otherTopic)
        every { flashcardRepository.findById(12L) } returns java.util.Optional.of(flashcard)

        assertThatThrownBy {
            flashcardService.updateFlashcard(topic.id!!, 12L, UpdateFlashcardRequest("Q", "A"))
        }
            .isInstanceOf(ResponseStatusException::class.java)
            .hasMessageContaining("does not belong to topic")
    }

    @Test
    fun `updateFlashcard should reject blank data`() {
        val flashcard = Flashcard(id = 13L, question = "Old", answer = "Answer", topic = topic)
        every { flashcardRepository.findById(13L) } returns java.util.Optional.of(flashcard)

        assertThatThrownBy {
            flashcardService.updateFlashcard(topic.id!!, 13L, UpdateFlashcardRequest(" ", "A"))
        }
            .isInstanceOf(ResponseStatusException::class.java)
            .hasMessageContaining("must not be blank")
    }
}

private class NoOpTelemetry : TelemetryFacade {
    private val span = object : TelemetrySpan {
        override val traceId: String = "trace"
        override val spanId: String = "span"
    }
    private val counterHandle = object : CounterHandle {
        override fun increment(amount: Double) {}
    }
    private val timerHandle = object : TimerHandle {
        override fun record(durationMillis: Long) {}
        override suspend fun <T> recordSuspend(block: suspend () -> T): T = block()
    }
    private val gaugeHandle = GaugeHandle { _ -> }

    override fun counter(name: String, tags: Map<String, String>): CounterHandle = counterHandle

    override fun timer(name: String, tags: Map<String, String>): TimerHandle = timerHandle

    override fun gauge(name: String, tags: Map<String, String>): GaugeHandle = gaugeHandle

    override fun startSpan(name: String, kind: SpanKind, attributes: Map<String, Any?>): SpanScope =
        object : SpanScope {
            override val span: TelemetrySpan
                get() = this@NoOpTelemetry.span

            override fun close() {}
        }

    override suspend fun <T> inSpan(
        name: String,
        kind: SpanKind,
        attributes: Map<String, Any?>,
        block: suspend (TelemetrySpan) -> T,
    ): T = block(span)

    override fun prometheusScrape(): String? = null

    override fun shutdown() {}
}
