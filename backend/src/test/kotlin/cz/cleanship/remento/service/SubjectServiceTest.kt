package cz.cleanship.remento.service

import cz.cleanship.remento.common.dto.CreateSubjectRequest
import cz.cleanship.remento.common.dto.UpdateSubjectRequest
import cz.cleanship.remento.domain.Subject
import cz.cleanship.remento.repository.SubjectRepository
import cz.cleanship.remento.testsupport.NoOpTelemetry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.web.server.ResponseStatusException
import java.util.Optional

class SubjectServiceTest {

    private val subjectRepository: SubjectRepository = mockk()
    private val subjectService = SubjectService(subjectRepository, NoOpTelemetry())

    @Test
    fun `getAll should return all subjects`() {
        val subject = Subject(id = 1L, name = "Math")
        every { subjectRepository.findAll() } returns listOf(subject)

        val result = subjectService.getAll()

        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("Math")
    }

    @Test
    fun `getOne should return subject by ID`() {
        val subject = Subject(id = 1L, name = "Math")
        every { subjectRepository.findById(1L) } returns Optional.of(subject)

        val result = subjectService.getOne(1L)

        assertThat(result.name).isEqualTo("Math")
    }

    @Test
    fun `create should save and return subject`() {
        val request = CreateSubjectRequest(name = "Math")
        val subject = Subject(id = 1L, name = "Math")
        every { subjectRepository.save(any()) } returns subject

        val result = subjectService.create(request)

        assertThat(result.name).isEqualTo("Math")
        verify { subjectRepository.save(match { it.name == "Math" }) }
    }

    @Test
    fun `update should modify and save subject`() {
        val subject = Subject(id = 1L, name = "Math")
        val request = UpdateSubjectRequest(name = "Advanced Math")
        every { subjectRepository.findById(1L) } returns Optional.of(subject)
        every { subjectRepository.save(subject) } returns subject

        val result = subjectService.update(1L, request)

        assertThat(result.name).isEqualTo("Advanced Math")
        assertThat(subject.name).isEqualTo("Advanced Math")
        verify { subjectRepository.save(subject) }
    }

    @Test
    fun `delete should remove subject`() {
        every { subjectRepository.existsById(1L) } returns true
        every { subjectRepository.deleteById(1L) } returns Unit

        subjectService.delete(1L)

        verify { subjectRepository.deleteById(1L) }
    }
}
