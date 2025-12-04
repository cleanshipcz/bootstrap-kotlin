package cz.cleanship.remento.controller

import cz.cleanship.remento.common.dto.CreateFlashcardRequest
import cz.cleanship.remento.common.dto.FlashcardDto
import cz.cleanship.remento.common.dto.UpdateFlashcardRequest
import cz.cleanship.remento.service.IFlashcardService
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
@RequestMapping("/api/topics/{topicId}/flashcards")
@CrossOrigin(origins = ["http://localhost:3000", "http://localhost:5173"])
open class FlashcardController(
    private val flashcardService: IFlashcardService,
) {
    @GetMapping
    fun getFlashcards(@PathVariable topicId: Long): ResponseEntity<List<FlashcardDto>> =
        ResponseEntity.ok(flashcardService.getFlashcards(topicId))

    @PostMapping
    fun createFlashcard(
        @PathVariable topicId: Long,
        @RequestBody request: CreateFlashcardRequest,
    ): ResponseEntity<FlashcardDto> {
        val flashcard = flashcardService.createFlashcard(topicId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(flashcard)
    }

    @DeleteMapping("/{flashcardId}")
    fun deleteFlashcard(
        @PathVariable topicId: Long,
        @PathVariable flashcardId: Long,
    ): ResponseEntity<Void> {
        flashcardService.deleteFlashcard(topicId, flashcardId)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/{flashcardId}")
    fun updateFlashcard(
        @PathVariable topicId: Long,
        @PathVariable flashcardId: Long,
        @RequestBody request: UpdateFlashcardRequest,
    ): ResponseEntity<FlashcardDto> =
        ResponseEntity.ok(flashcardService.updateFlashcard(topicId, flashcardId, request))
}
