package cz.cleanship.remento.controller

import cz.cleanship.remento.common.dto.CreateSubjectRequest
import cz.cleanship.remento.common.dto.SubjectDto
import cz.cleanship.remento.common.dto.TopicSummaryDto
import cz.cleanship.remento.common.dto.UpdateSubjectRequest
import cz.cleanship.remento.config.RestExceptionHandler
import cz.cleanship.remento.service.SubjectService
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

class SubjectControllerTest {

    private val subjectService: SubjectService = mock(SubjectService::class.java)
    private val mockMvc: MockMvc = MockMvcBuilders
        .standaloneSetup(SubjectController(subjectService))
        .setControllerAdvice(RestExceptionHandler())
        .build()

    @Test
    fun `getSubjects returns list`() {
        val payload = listOf(SubjectDto(id = 1L, name = "Math", topics = listOf(TopicSummaryDto(2L, "Algebra"))))
        doReturn(payload).`when`(subjectService).getAll()

        mockMvc.perform(get("/api/subjects"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Math"))
            .andExpect(jsonPath("$[0].topics[0].name").value("Algebra"))
    }

    @Test
    fun `createSubject returns created`() {
        val response = SubjectDto(id = 10L, name = "History", topics = emptyList())
        doReturn(response).`when`(subjectService).create(CreateSubjectRequest("History"))

        mockMvc.perform(
            post("/api/subjects")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"History"}"""),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(10))
            .andExpect(jsonPath("$.name").value("History"))
    }

    @Test
    fun `updateSubject delegates to service`() {
        val response = SubjectDto(id = 5L, name = "Updated", topics = emptyList())
        doReturn(response).`when`(subjectService).update(5L, UpdateSubjectRequest("Updated"))

        mockMvc.perform(
            put("/api/subjects/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Updated"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Updated"))
    }

    @Test
    fun `deleteSubject returns no content`() {
        mockMvc.perform(delete("/api/subjects/9"))
            .andExpect(status().isNoContent)

        verify(subjectService).delete(9L)
    }
}
