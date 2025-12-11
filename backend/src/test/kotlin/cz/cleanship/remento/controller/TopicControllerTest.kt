package cz.cleanship.remento.controller

import cz.cleanship.remento.common.dto.CreateTopicRequest
import cz.cleanship.remento.common.dto.TopicDto
import cz.cleanship.remento.common.dto.TopicSummaryDto
import cz.cleanship.remento.common.dto.UpdateTopicRequest
import cz.cleanship.remento.config.RestExceptionHandler
import cz.cleanship.remento.service.TopicService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class TopicControllerTest {

    private val topicService: TopicService = mock(TopicService::class.java)
    private val mockMvc: MockMvc = MockMvcBuilders
        .standaloneSetup(TopicController(topicService))
        .setControllerAdvice(RestExceptionHandler())
        .build()

    @Test
    fun `getTopics returns list`() {
        val topics = listOf(
            TopicDto(id = 2L, subjectId = 1L, name = "Algebra", studyPassage = "notes", flashcards = emptyList(), flashcardCount = 0),
        )
        doReturn(topics).`when`(topicService).getTopics(1L)

        mockMvc.perform(get("/api/subjects/1/topics"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Algebra"))
    }

    @Test
    fun `getTopic returns detail`() {
        val topic = TopicDto(id = 2L, subjectId = 1L, name = "Geometry", studyPassage = "Triangles", flashcards = emptyList(), flashcardCount = 0)
        doReturn(topic).`when`(topicService).getOne(2L)

        mockMvc.perform(get("/api/topics/2"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Geometry"))
            .andExpect(jsonPath("$.studyPassage").value("Triangles"))
    }

    @Test
    fun `createTopic returns created`() {
        val created = TopicDto(id = 5L, subjectId = 1L, name = "Botany", studyPassage = "plants", flashcards = emptyList(), flashcardCount = 0)
        val request = CreateTopicRequest(subjectId = 1L, name = "Botany", studyPassage = "plants")
        doReturn(created).`when`(topicService).create(request)

        mockMvc.perform(
            post("/api/subjects/1/topics")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Botany","studyPassage":"plants"}"""),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(5))
            .andExpect(jsonPath("$.name").value("Botany"))
    }

    @Test
    fun `updateTopic returns updated`() {
        val updated = TopicDto(id = 7L, subjectId = 1L, name = "Updated", studyPassage = "notes", flashcards = emptyList(), flashcardCount = 0)
        doReturn(updated).`when`(topicService).update(7L, UpdateTopicRequest("Updated", "notes"))

        mockMvc.perform(
            put("/api/topics/7")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Updated","studyPassage":"notes"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Updated"))
    }

    @Test
    fun `deleteTopic returns no content`() {
        mockMvc.perform(delete("/api/topics/9"))
            .andExpect(status().isNoContent)

        verify(topicService).delete(9L)
    }
}
