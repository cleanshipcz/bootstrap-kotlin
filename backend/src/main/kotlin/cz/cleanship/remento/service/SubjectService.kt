package cz.cleanship.remento.service

import cz.cleanship.remento.common.dto.CreateSubjectRequest
import cz.cleanship.remento.common.dto.SubjectDto
import cz.cleanship.remento.common.dto.UpdateSubjectRequest
import cz.cleanship.remento.domain.Subject
import cz.cleanship.remento.mapper.toDto
import cz.cleanship.remento.repository.SubjectRepository
import cz.cleanship.telemetry.TelemetryFacade
import org.springframework.stereotype.Service

@Service
class SubjectService(
    private val subjectRepository: SubjectRepository,
    telemetry: TelemetryFacade,
) : BaseCrudService<Subject, Long, CreateSubjectRequest, UpdateSubjectRequest, SubjectDto>(
    "Subject", subjectRepository, telemetry
) {

    override fun createEntity(req: CreateSubjectRequest): Subject =
        Subject(name = req.name)

    override fun updateEntity(entity: Subject, req: UpdateSubjectRequest) {
        entity.name = req.name
    }

    override fun toDto(entity: Subject): SubjectDto = entity.toDto()
}
