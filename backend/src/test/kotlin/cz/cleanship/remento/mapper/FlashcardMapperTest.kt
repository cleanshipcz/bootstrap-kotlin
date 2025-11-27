package cz.cleanship.remento.mapper

import cz.cleanship.remento.domain.Flashcard
import cz.cleanship.remento.domain.Subject
import cz.cleanship.remento.domain.Topic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FlashcardMapperTest {

    @Test
    fun `toDto should map Flashcard to FlashcardDto correctly`() {
        // Given
        val subject = Subject(id = 1L, name = "Languages")
        val topic = Topic(id = 2L, name = "Kotlin", studyPassage = "Study", subject = subject)
        val flashcard = Flashcard(
            id = 123L,
            question = "Test Question",
            answer = "Test Answer",
            topic = topic,
        )

        // When
        val dto = flashcard.toDto()

        // Then
        assertThat(dto.id).isEqualTo(123L)
        assertThat(dto.topicId).isEqualTo(2L)
        assertThat(dto.question).isEqualTo("Test Question")
        assertThat(dto.answer).isEqualTo("Test Answer")
    }

    @Test
    fun `toDto should handle null id`() {
        val subject = Subject(id = 1L, name = "Languages")
        val topic = Topic(id = 2L, name = "Kotlin", studyPassage = "Study", subject = subject)
        val flashcard = Flashcard(
            id = null,
            question = "Test Question",
            answer = "Test Answer",
            topic = topic,
        )

        // When
        val dto = flashcard.toDto()

        // Then
        assertThat(dto.id).isNull()
        assertThat(dto.topicId).isEqualTo(2L)
        assertThat(dto.question).isEqualTo("Test Question")
        assertThat(dto.answer).isEqualTo("Test Answer")
    }
}
