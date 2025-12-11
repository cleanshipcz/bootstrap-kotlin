import { apiClient } from './client'
import type { CreateSubjectRequest, SubjectDto, UpdateSubjectRequest } from './types'

export const subjectsApi = {
  getAll: (): Promise<SubjectDto[]> => {
    return apiClient.request('/subjects')
  },

  getOne: (subjectId: number): Promise<SubjectDto> => {
    return apiClient.request(`/subjects/${subjectId}`)
  },

  create: (request: CreateSubjectRequest): Promise<SubjectDto> => {
    return apiClient.request('/subjects', {
      method: 'POST',
      body: JSON.stringify(request),
    })
  },

  update: (subjectId: number, request: UpdateSubjectRequest): Promise<SubjectDto> => {
    return apiClient.request(`/subjects/${subjectId}`, {
      method: 'PUT',
      body: JSON.stringify(request),
    })
  },

  delete: (subjectId: number): Promise<void> => {
    return apiClient.request(`/subjects/${subjectId}`, {
      method: 'DELETE',
    })
  },
}
