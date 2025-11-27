import type {
  ApiErrorResponse,
  CreateFlashcardRequest,
  CreateSubjectRequest,
  CreateTopicRequest,
  FlashcardDto,
  SubjectDto,
  TopicDto,
  UpdateSubjectRequest,
  UpdateTopicRequest,
} from './types'

const API_BASE_URL = '/api'

export class ApiError extends Error {
  status: number
  body: ApiErrorResponse | undefined

  constructor(status: number, body?: ApiErrorResponse) {
    super(body?.message ?? `Request failed with status ${status}`)
    this.status = status
    this.body = body
  }
}

class ApiClient {
  private async request<T>(path: string, init?: RequestInit): Promise<T> {
    const response = await fetch(`${API_BASE_URL}${path}`, {
      headers: {
        'Content-Type': 'application/json',
        ...(init?.headers ?? {}),
      },
      ...init,
    })

    if (!response.ok) {
      let errorBody: ApiErrorResponse | undefined
      try {
        errorBody = await response.json()
      } catch {
        // ignore
      }
      throw new ApiError(response.status, errorBody)
    }

    if (response.status === 204) {
      return undefined as T
    }

    return response.json()
  }

  // Subjects
  getSubjects(): Promise<SubjectDto[]> {
    return this.request('/subjects')
  }

  createSubject(request: CreateSubjectRequest): Promise<SubjectDto> {
    return this.request('/subjects', {
      method: 'POST',
      body: JSON.stringify(request),
    })
  }

  updateSubject(subjectId: number, request: UpdateSubjectRequest): Promise<SubjectDto> {
    return this.request(`/subjects/${subjectId}`, {
      method: 'PUT',
      body: JSON.stringify(request),
    })
  }

  deleteSubject(subjectId: number): Promise<void> {
    return this.request(`/subjects/${subjectId}`, {
      method: 'DELETE',
    })
  }

  // Topics
  getTopics(subjectId: number): Promise<TopicDto[]> {
    return this.request(`/subjects/${subjectId}/topics`)
  }

  getTopic(topicId: number): Promise<TopicDto> {
    return this.request(`/topics/${topicId}`)
  }

  createTopic(subjectId: number, request: CreateTopicRequest): Promise<TopicDto> {
    return this.request(`/subjects/${subjectId}/topics`, {
      method: 'POST',
      body: JSON.stringify(request),
    })
  }

  updateTopic(topicId: number, request: UpdateTopicRequest): Promise<TopicDto> {
    return this.request(`/topics/${topicId}`, {
      method: 'PUT',
      body: JSON.stringify(request),
    })
  }

  deleteTopic(topicId: number): Promise<void> {
    return this.request(`/topics/${topicId}`, {
      method: 'DELETE',
    })
  }

  // Flashcards
  getFlashcards(topicId: number): Promise<FlashcardDto[]> {
    return this.request(`/topics/${topicId}/flashcards`)
  }

  createFlashcard(topicId: number, request: CreateFlashcardRequest): Promise<FlashcardDto> {
    return this.request(`/topics/${topicId}/flashcards`, {
      method: 'POST',
      body: JSON.stringify(request),
    })
  }

  deleteFlashcard(topicId: number, flashcardId: number): Promise<void> {
    return this.request(`/topics/${topicId}/flashcards/${flashcardId}`, {
      method: 'DELETE',
    })
  }
}

export const apiClient = new ApiClient()
