package cz.cleanship.remento.service

import cz.cleanship.remento.common.dto.CreateSubjectRequest
import cz.cleanship.remento.common.dto.SubjectDto
import cz.cleanship.remento.common.dto.UpdateSubjectRequest
import cz.cleanship.remento.mapper.toDto
import cz.cleanship.remento.repository.SubjectRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

interface ISubjectService {
    fun getAllSubjects(): List<SubjectDto>
    fun getSubject(subjectId: Long): SubjectDto
    fun createSubject(request: CreateSubjectRequest): SubjectDto
    fun updateSubject(subjectId: Long, request: UpdateSubjectRequest): SubjectDto
    fun deleteSubject(subjectId: Long)
}

@Service
@Transactional
open class SubjectService(
    private val subjectRepository: SubjectRepository,
) : ISubjectService {

    override fun getAllSubjects(): List<SubjectDto> =
        subjectRepository.findAll().map { it.toDto(includeTopics = true) }

    override fun getSubject(subjectId: Long): SubjectDto =
        findSubject(subjectId).toDto(includeTopics = true)

    override fun createSubject(request: CreateSubjectRequest): SubjectDto {
        val name = request.name.trim()
        validateName(name)
        val subject = subjectRepository.save(
            cz.cleanship.remento.domain.Subject(
                name = name,
            ),
        )
        return subject.toDto(includeTopics = true)
    }

    override fun updateSubject(subjectId: Long, request: UpdateSubjectRequest): SubjectDto {
        val subject = findSubject(subjectId)
        val name = request.name.trim()
        validateName(name)
        subject.name = name
        return subjectRepository.save(subject).toDto(includeTopics = true)
    }

    override fun deleteSubject(subjectId: Long) {
        if (!subjectRepository.existsById(subjectId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Subject $subjectId not found")
        }
        subjectRepository.deleteById(subjectId)
    }

    private fun findSubject(subjectId: Long) =
        subjectRepository.findById(subjectId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Subject $subjectId not found") }

    private fun validateName(name: String) {
        if (name.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject name must not be blank")
        }
    }
}

