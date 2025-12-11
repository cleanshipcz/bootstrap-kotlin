package cz.cleanship.remento.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import cz.cleanship.remento.common.dto.CreateFlashcardRequest
import cz.cleanship.remento.common.dto.FlashcardDto
import cz.cleanship.remento.config.RestExceptionHandler
import cz.cleanship.remento.service.FlashcardService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class FlashcardControllerTest {

    private val flashcardService: FlashcardService = mock(FlashcardService::class.java)
    private val mockMvc: MockMvc = MockMvcBuilders
        .standaloneSetup(FlashcardController(flashcardService))
        .setControllerAdvice(RestExceptionHandler())
        .build()

    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `getFlashcards should return list of flashcards`() {
        val topicId = 10L
        val flashcards = listOf(
            FlashcardDto(1L, topicId, 99L, "Q1", "A1"),
            FlashcardDto(2L, topicId, 99L, "Q2", "A2"),
        )
        doReturn(flashcards).`when`(flashcardService).getFlashcards(topicId)

        mockMvc
            .perform(get("/api/topics/$topicId/flashcards"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].question").value("Q1"))
            .andExpect(jsonPath("$[1].question").value("Q2"))
    }

    @Test
    fun `createFlashcard should create and return flashcard`() {
        val topicId = 5L
        // The body does not contain topicId, but service call expects request with topicId injected by controller
        val requestBody = CreateFlashcardRequest(topicId = null, question = "Q", answer = "A")
        val requestWithTopic = CreateFlashcardRequest(topicId = topicId, question = "Q", answer = "A")
        val responseDto = FlashcardDto(1L, topicId, 42L, "Q", "A")

        doReturn(responseDto)
            .`when`(flashcardService)
            .create(requestWithTopic)

        mockMvc
            .perform(
                post("/api/topics/$topicId/flashcards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.question").value("Q"))
            .andExpect(jsonPath("$.answer").value("A"))
    }

    @Test
    fun `deleteFlashcard should return no content`() {
        val topicId = 5L
        val flashcardId = 1L

        mockMvc
            .perform(delete("/api/topics/$topicId/flashcards/$flashcardId"))
            .andExpect(status().isNoContent)

        verify(flashcardService).delete(flashcardId)
    }

    @Test
    fun `deleteFlashcard propagates not found`() {
        val topicId = 5L
        val flashcardId = 99L
        `when`(flashcardService.delete(flashcardId)).thenThrow(
            org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND),
        )

        mockMvc
            .perform(delete("/api/topics/$topicId/flashcards/$flashcardId"))
            .andExpect(status().isNotFound)
    }
}
