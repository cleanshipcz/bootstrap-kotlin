package cz.cleanship.remento.common.dto

data class SubjectDto(
    val id: Long?,
    val name: String,
    val topics: List<TopicSummaryDto> = emptyList(),
)

data class TopicSummaryDto(
    val id: Long?,
    val name: String,
)

data class CreateSubjectRequest(
    val name: String,
)

data class UpdateSubjectRequest(
    val name: String,
)

data class TopicDto(
    val id: Long?,
    val subjectId: Long,
    val name: String,
    val studyPassage: String,
    val flashcards: List<FlashcardDto> = emptyList(),
    val flashcardCount: Int = flashcards.size,
)

data class CreateTopicRequest(
    val name: String,
    val studyPassage: String,
)

data class UpdateTopicRequest(
    val name: String,
    val studyPassage: String,
)

data class FlashcardDto(
    val id: Long?,
    val topicId: Long,
    val question: String,
    val answer: String,
)

data class CreateFlashcardRequest(
    val question: String,
    val answer: String,
)

data class UpdateFlashcardRequest(
    val question: String,
    val answer: String,
)
