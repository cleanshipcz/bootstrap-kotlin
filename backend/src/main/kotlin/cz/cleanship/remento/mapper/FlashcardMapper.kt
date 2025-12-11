package cz.cleanship.remento.mapper

import cz.cleanship.remento.common.dto.FlashcardDto
import cz.cleanship.remento.common.dto.SubjectDto
import cz.cleanship.remento.common.dto.TopicDto
import cz.cleanship.remento.common.dto.TopicSummaryDto
import cz.cleanship.remento.domain.Flashcard
import cz.cleanship.remento.domain.Subject
import cz.cleanship.remento.domain.Topic

fun Flashcard.toDto(): FlashcardDto = FlashcardDto(
    id = id,
    topicId = topic?.id ?: error("Flashcard $id is not associated with a topic"),
    subjectId = subject?.id ?: topic?.subject?.id ?: error("Flashcard $id is not associated with a subject"),
    question = question,
    answer = answer,
)

fun Topic.toSummaryDto(): TopicSummaryDto = TopicSummaryDto(
    id = id,
    name = name,
)

fun Topic.toDto(includeFlashcards: Boolean = true): TopicDto {
    val flashcardsPayload = if (includeFlashcards) {
        flashcards.map { it.toDto() }
    } else {
        emptyList()
    }

    return TopicDto(
    id = id,
    subjectId = subject?.id ?: error("Topic $id is not associated with a subject"),
    name = name,
    studyPassage = studyPassage,
        flashcards = flashcardsPayload,
        flashcardCount = flashcards.size,
)
}

fun Subject.toDto(includeTopics: Boolean = true): SubjectDto = SubjectDto(
    id = id,
    name = name,
    topics = if (includeTopics) topics.map { it.toSummaryDto() } else emptyList(),
)
