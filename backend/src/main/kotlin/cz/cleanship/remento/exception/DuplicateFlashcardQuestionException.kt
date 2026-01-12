package cz.cleanship.remento.exception

class DuplicateFlashcardQuestionException(
    topicId: Long,
    question: String,
) : RuntimeException("Flashcard question \"$question\" already exists in topic $topicId")

