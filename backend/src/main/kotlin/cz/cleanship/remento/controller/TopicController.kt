package cz.cleanship.remento.controller

import cz.cleanship.remento.common.dto.CreateTopicRequest
import cz.cleanship.remento.common.dto.TopicDto
import cz.cleanship.remento.common.dto.UpdateTopicRequest
import cz.cleanship.remento.service.ITopicService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = ["http://localhost:3000", "http://localhost:5173"])
class TopicController(
    private val topicService: ITopicService,
) {

    @GetMapping("/api/subjects/{subjectId}/topics")
    fun getTopics(@PathVariable subjectId: Long): ResponseEntity<List<TopicDto>> =
        ResponseEntity.ok(topicService.getTopics(subjectId))

    @PostMapping("/api/subjects/{subjectId}/topics")
    fun createTopic(
        @PathVariable subjectId: Long,
        @RequestBody request: CreateTopicRequest,
    ): ResponseEntity<TopicDto> =
        ResponseEntity.status(HttpStatus.CREATED).body(topicService.createTopic(subjectId, request))

    @GetMapping("/api/topics/{topicId}")
    fun getTopic(@PathVariable topicId: Long): ResponseEntity<TopicDto> =
        ResponseEntity.ok(topicService.getTopic(topicId))

    @PutMapping("/api/topics/{topicId}")
    fun updateTopic(
        @PathVariable topicId: Long,
        @RequestBody request: UpdateTopicRequest,
    ): ResponseEntity<TopicDto> = ResponseEntity.ok(topicService.updateTopic(topicId, request))

    @DeleteMapping("/api/topics/{topicId}")
    fun deleteTopic(@PathVariable topicId: Long): ResponseEntity<Void> {
        topicService.deleteTopic(topicId)
        return ResponseEntity.noContent().build()
    }
}

