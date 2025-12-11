package cz.cleanship.remento.repository

import cz.cleanship.remento.domain.Topic
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TopicRepository : JpaRepository<Topic, Long> {
    fun findBySubject_Id(subjectId: Long): List<Topic>
}

