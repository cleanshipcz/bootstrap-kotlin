package cz.cleanship.remento.repository

import cz.cleanship.remento.domain.Flashcard
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FlashcardRepository : JpaRepository<Flashcard, Long> {
    fun findByTopicId(topicId: Long): List<Flashcard>
}
