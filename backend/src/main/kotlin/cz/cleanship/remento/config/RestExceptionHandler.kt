package cz.cleanship.remento.config

import cz.cleanship.remento.exception.DuplicateFlashcardQuestionException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException

@RestControllerAdvice
class RestExceptionHandler {

    data class ErrorResponse(
        val status: Int,
        val error: String,
        val message: String?,
    )

    @ExceptionHandler(DuplicateFlashcardQuestionException::class)
    fun handleDuplicate(e: DuplicateFlashcardQuestionException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(
                ErrorResponse(
                    status = HttpStatus.CONFLICT.value(),
                    error = "DuplicateFlashcardQuestion",
                    message = e.message,
                ),
            )

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatus(e: ResponseStatusException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(e.statusCode)
            .body(
                ErrorResponse(
                    status = e.statusCode.value(),
                    error = e.statusCode.toString(),
                    message = e.reason,
                ),
            )
}

