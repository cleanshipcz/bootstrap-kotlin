import { ApiErrorResponse } from './types'

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

export class ApiClient {
  async request<T>(path: string, init?: RequestInit): Promise<T> {
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
}

export const apiClient = new ApiClient()
