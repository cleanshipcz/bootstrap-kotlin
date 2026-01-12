export interface SubjectDto {
  id: number | null
  name: string
  topics: TopicSummaryDto[]
}

export interface TopicSummaryDto {
  id: number | null
  name: string
}

export interface TopicDto {
  id: number | null
  subjectId: number
  name: string
  studyPassage: string
  flashcards: FlashcardDto[]
  flashcardCount: number
}

export interface CreateSubjectRequest {
  name: string
}

export interface UpdateSubjectRequest {
  name: string
}

export interface CreateTopicRequest {
  name: string
  studyPassage: string
}

export interface UpdateTopicRequest {
  name: string
  studyPassage: string
}

export interface FlashcardDto {
  id: number | null
  topicId: number
  question: string
  answer: string
}

export interface CreateFlashcardRequest {
  question: string
  answer: string
}

export interface UpdateFlashcardRequest {
  question: string
  answer: string
}

export interface ApiErrorResponse {
  status: number
  error: string
  message?: string
}
