import { apiClient } from './client'
import type { CreateTopicRequest, TopicDto, UpdateTopicRequest } from './types'

export const topicsApi = {
  getAll: (subjectId: number): Promise<TopicDto[]> => {
    return apiClient.request(`/subjects/${subjectId}/topics`)
  },

  getOne: (topicId: number): Promise<TopicDto> => {
    return apiClient.request(`/topics/${topicId}`)
  },

  create: (subjectId: number, request: CreateTopicRequest): Promise<TopicDto> => {
    return apiClient.request(`/subjects/${subjectId}/topics`, {
      method: 'POST',
      body: JSON.stringify(request),
    })
  },

  update: (topicId: number, request: UpdateTopicRequest): Promise<TopicDto> => {
    return apiClient.request(`/topics/${topicId}`, {
      method: 'PUT',
      body: JSON.stringify(request),
    })
  },

  delete: (topicId: number): Promise<void> => {
    return apiClient.request(`/topics/${topicId}`, {
      method: 'DELETE',
    })
  },
}

