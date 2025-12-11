package cz.cleanship.remento.integration

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import cz.cleanship.remento.common.dto.CreateFlashcardRequest
import cz.cleanship.remento.common.dto.FlashcardDto
import cz.cleanship.remento.common.dto.CreateSubjectRequest
import cz.cleanship.remento.common.dto.CreateTopicRequest
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class FlashcardIntegrationTest(
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

    private var subjectId: Long = 0
    private var topicId: Long = 0

    @BeforeEach
    fun setupData() {
        flashcardRepository.deleteAll()
        topicRepository.deleteAll()
        subjectRepository.deleteAll()

        val subjectResponse = mockMvc.perform(
            post("/api/subjects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateSubjectRequest("Languages"))),
        )
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .contentAsString
        val subject = objectMapper.readValue(subjectResponse, object : TypeReference<cz.cleanship.remento.common.dto.SubjectDto>() {})
        subjectId = subject.id!!

        val topicResponse = mockMvc.perform(
            post("/api/subjects/$subjectId/topics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateTopicRequest(name = "Kotlin", studyPassage = "About Kotlin"))),
        )
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .contentAsString
        val topic = objectMapper.readValue(topicResponse, cz.cleanship.remento.common.dto.TopicDto::class.java)
        topicId = topic.id!!
    }

    @AfterEach
    fun cleanDatabase() {
        flashcardRepository.deleteAll()
        topicRepository.deleteAll()
        subjectRepository.deleteAll()
    }

    @Test
    fun `create two flashcards and delete one`() {
        val cardOne = createFlashcard("What is Kotlin?", "A modern JVM language.")
        val cardTwo = createFlashcard("Capital of France?", "Paris")

        val all = listFlashcards()
        assertThat(all).extracting<Long?> { it.id }.contains(cardOne.id, cardTwo.id)

        deleteFlashcard(cardOne.id!!)

        val remaining = listFlashcards()
        assertThat(remaining).hasSize(1)
        assertThat(remaining.first().id).isEqualTo(cardTwo.id)
    }

    @Test
    fun `deleting non-existent flashcard returns 404`() {
        mockMvc.perform(delete("/api/topics/$topicId/flashcards/99999"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `second delete on same flashcard returns 404`() {
        val flashcard = createFlashcard("Question", "Answer")
        deleteFlashcard(flashcard.id!!)

        mockMvc.perform(delete("/api/topics/$topicId/flashcards/${flashcard.id}"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `list flashcards returns empty array when none exist`() {
        val result = listFlashcards()
        assertThat(result).isEmpty()
    }

    private fun createFlashcard(question: String, answer: String): FlashcardDto {
        val request = CreateFlashcardRequest(question = question, answer = answer)
        val content = objectMapper.writeValueAsString(request)

        val response = mockMvc.perform(
            post("/api/topics/$topicId/flashcards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content),
        )
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .contentAsString

        return objectMapper.readValue(response, FlashcardDto::class.java)
    }

    private fun listFlashcards(): List<FlashcardDto> {
        val response = mockMvc.perform(get("/api/topics/$topicId/flashcards"))
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString

        return objectMapper.readValue(response, object : TypeReference<List<FlashcardDto>>() {})
    }

    private fun deleteFlashcard(id: Long) {
        mockMvc.perform(delete("/api/topics/$topicId/flashcards/$id"))
            .andExpect(status().isNoContent)
    }
}
