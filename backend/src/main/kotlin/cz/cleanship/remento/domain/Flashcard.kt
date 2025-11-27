package cz.cleanship.remento.domain

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

@Entity
@Table(
    name = "flashcards",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_flashcard_question_topic", columnNames = ["question", "topic_id"]),
    ],
)
data class Flashcard(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var question: String = "",
    var answer: String = "",
    var createdAt: Instant = Instant.now(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    var topic: Topic? = null,
) {
    @Suppress("unused")
    constructor() : this(
        id = null,
        question = "",
        answer = "",
        createdAt = Instant.now(),
        topic = null,
    )
}
