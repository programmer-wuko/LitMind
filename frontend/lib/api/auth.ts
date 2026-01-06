import { apiClient } from './client'

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  email?: string
  nickname?: string
  departmentId?: number
  newDepartmentName?: string
}

export interface LoginResponse {
  code: number
  message: string
  data: {
    token: string
    username: string
    userId: number
  }
}

export const authApi = {
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    return apiClient.post('/auth/login', credentials)
  },

  register: async (request: RegisterRequest): Promise<LoginResponse> => {
    return apiClient.post('/auth/register', request)
  },
}

