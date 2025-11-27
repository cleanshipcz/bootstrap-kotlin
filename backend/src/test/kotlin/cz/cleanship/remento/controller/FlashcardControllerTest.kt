package cz.cleanship.remento.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import cz.cleanship.remento.common.dto.CreateFlashcardRequest
import cz.cleanship.remento.common.dto.FlashcardDto
import cz.cleanship.remento.service.IFlashcardService
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(FlashcardController::class)
class FlashcardControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var flashcardService: IFlashcardService

    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `getFlashcards should return list of flashcards`() {
        val topicId = 10L
        val flashcards = listOf(
            FlashcardDto(1L, topicId, "Q1", "A1"),
            FlashcardDto(2L, topicId, "Q2", "A2"),
        )
        `when`(flashcardService.getFlashcards(topicId)).thenReturn(flashcards)

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
        val request = CreateFlashcardRequest("Q", "A")
        val responseDto = FlashcardDto(1L, topicId, "Q", "A")

        `when`(flashcardService.createFlashcard(eq(topicId), any(CreateFlashcardRequest::class.java))).thenReturn(responseDto)

        mockMvc
            .perform(
                post("/api/topics/$topicId/flashcards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
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

        verify(flashcardService).deleteFlashcard(topicId, flashcardId)
    }
}
