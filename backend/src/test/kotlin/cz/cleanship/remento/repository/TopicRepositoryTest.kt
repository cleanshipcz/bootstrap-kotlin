package cz.cleanship.remento.repository

import cz.cleanship.remento.domain.Subject
import cz.cleanship.remento.domain.Topic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

@DataJpaTest
class TopicRepositoryTest(
    @Autowired private val entityManager: TestEntityManager,
    @Autowired private val topicRepository: TopicRepository,
) {
    @Test
    fun `findBySubject_Id returns topics for subject`() {
        val subject = entityManager.persist(Subject(name = "Algorithms"))
        val topic1 = entityManager.persist(Topic(name = "Sorting", studyPassage = "notes", subject = subject))
        val topic2 = entityManager.persist(Topic(name = "Graphs", studyPassage = "notes", subject = subject))
        entityManager.flush()

        val result = topicRepository.findBySubject_Id(subject.id!!)

        assertThat(result.map { it.id }).containsExactlyInAnyOrder(topic1.id, topic2.id)
        assertThat(result.map { it.subject?.id }.distinct()).containsExactly(subject.id)
    }
}


