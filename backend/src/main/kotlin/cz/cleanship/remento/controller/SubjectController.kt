package cz.cleanship.remento.controller

import cz.cleanship.remento.common.dto.CreateSubjectRequest
import cz.cleanship.remento.common.dto.SubjectDto
import cz.cleanship.remento.common.dto.UpdateSubjectRequest
import cz.cleanship.remento.service.SubjectService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/subjects")
@CrossOrigin(origins = ["http://localhost:3000", "http://localhost:5173"])
class SubjectController(
    private val subjectService: SubjectService,
) {

    @GetMapping
    fun getSubjects(): ResponseEntity<List<SubjectDto>> =
        ResponseEntity.ok(subjectService.getAll())

    @GetMapping("/{subjectId}")
    fun getSubject(@PathVariable subjectId: Long): ResponseEntity<SubjectDto> =
        ResponseEntity.ok(subjectService.getOne(subjectId))

    @PostMapping
    fun createSubject(@RequestBody request: CreateSubjectRequest): ResponseEntity<SubjectDto> =
        ResponseEntity.status(HttpStatus.CREATED).body(subjectService.create(request))

    @PutMapping("/{subjectId}")
    fun updateSubject(
        @PathVariable subjectId: Long,
        @RequestBody request: UpdateSubjectRequest,
    ): ResponseEntity<SubjectDto> = ResponseEntity.ok(subjectService.update(subjectId, request))

    @DeleteMapping("/{subjectId}")
    fun deleteSubject(@PathVariable subjectId: Long): ResponseEntity<Void> {
        subjectService.delete(subjectId)
        return ResponseEntity.noContent().build()
    }
}
