import { apiClient } from './client'
import type { CreateFlashcardRequest, FlashcardDto, UpdateFlashcardRequest } from './types'

export const flashcardsApi = {
  getAll: (topicId: number): Promise<FlashcardDto[]> => {
    return apiClient.request(`/topics/${topicId}/flashcards`)
  },

  create: (topicId: number, request: CreateFlashcardRequest): Promise<FlashcardDto> => {
    return apiClient.request(`/topics/${topicId}/flashcards`, {
      method: 'POST',
      body: JSON.stringify(request),
    })
  },

  update: (
    topicId: number,
    flashcardId: number,
    request: UpdateFlashcardRequest,
  ): Promise<FlashcardDto> => {
    return apiClient.request(`/topics/${topicId}/flashcards/${flashcardId}`, {
      method: 'PUT',
      body: JSON.stringify(request),
    })
  },

  delete: (topicId: number, flashcardId: number): Promise<void> => {
    return apiClient.request(`/topics/${topicId}/flashcards/${flashcardId}`, {
      method: 'DELETE',
    })
  },
}

