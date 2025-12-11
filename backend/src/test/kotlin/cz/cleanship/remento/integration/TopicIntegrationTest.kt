package cz.cleanship.remento.integration

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import cz.cleanship.remento.common.dto.CreateSubjectRequest
import cz.cleanship.remento.common.dto.CreateTopicRequest
import cz.cleanship.remento.common.dto.SubjectDto
import cz.cleanship.remento.common.dto.TopicDto
import cz.cleanship.remento.repository.FlashcardRepository
import cz.cleanship.remento.repository.SubjectRepository
import cz.cleanship.remento.repository.TopicRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class TopicIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val flashcardRepository: FlashcardRepository,
    @Autowired private val subjectRepository: SubjectRepository,
    @Autowired private val topicRepository: TopicRepository,
) {

    companion object {
        @Container
        private val postgres = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("remento_test")
            withUsername("remento")
            withPassword("remento")
        }

        @JvmStatic
        @DynamicPropertySource
        fun postgresProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @BeforeEach
    fun cleanDb() {
        flashcardRepository.deleteAll()
        topicRepository.deleteAll()
        subjectRepository.deleteAll()
    }

    @AfterEach
    fun after() {
        flashcardRepository.deleteAll()
        topicRepository.deleteAll()
        subjectRepository.deleteAll()
    }

    @Test
    fun `creating topic under subject is retrievable via subjects subjectId topics endpoint`() {
        val subject = createSubject("Algorithms")
        val createdTopic = createTopic(subject.id!!, "Sorting", "Study sorting algorithms")

        val topics = getTopics(subject.id!!)

        assertThat(topics).hasSize(1)
        assertThat(topics[0].id).isEqualTo(createdTopic.id)
        assertThat(topics[0].name).isEqualTo("Sorting")
        assertThat(topics[0].subjectId).isEqualTo(subject.id)
    }

    @Test
    fun `get subject by id includes topic summary count after creating topic`() {
        val subject = createSubject("Example")
        createTopic(subject.id!!, "Graphs", "Study graphs")

        val fetched = getSubject(subject.id!!)
        assertThat(fetched.id).isEqualTo(subject.id)
        assertThat(fetched.topics).hasSize(1)
        assertThat(fetched.topics[0].name).isEqualTo("Graphs")
    }

    private fun createSubject(name: String): SubjectDto {
        val response = mockMvc.perform(
            post("/api/subjects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateSubjectRequest(name))),
        )
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .contentAsString
        return objectMapper.readValue(response, SubjectDto::class.java)
    }

    private fun createTopic(subjectId: Long, name: String, studyPassage: String): TopicDto {
        val response = mockMvc.perform(
            post("/api/subjects/$subjectId/topics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateTopicRequest(name = name, studyPassage = studyPassage))),
        )
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .contentAsString
        return objectMapper.readValue(response, TopicDto::class.java)
    }

    private fun getTopics(subjectId: Long): List<TopicDto> {
        val response = mockMvc.perform(get("/api/subjects/$subjectId/topics"))
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
        return objectMapper.readValue(response, object : TypeReference<List<TopicDto>>() {})
    }

    private fun getSubject(subjectId: Long): SubjectDto {
        val response = mockMvc.perform(get("/api/subjects/$subjectId"))
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
        return objectMapper.readValue(response, SubjectDto::class.java)
    }
}


