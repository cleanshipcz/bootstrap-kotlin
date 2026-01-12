package cz.cleanship.remento.domain

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "topics")
data class Topic(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false)
    var name: String = "",
    @Column(nullable = false, length = 2000)
    var studyPassage: String = "",
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    var subject: Subject? = null,
    @OneToMany(
        mappedBy = "topic",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY,
    )
    var flashcards: MutableList<Flashcard> = mutableListOf(),
) {
    constructor() : this(id = null, name = "", studyPassage = "", subject = null, flashcards = mutableListOf())
}

