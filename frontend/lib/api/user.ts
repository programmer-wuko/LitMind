import { apiClient } from './client'

export interface UserInfo {
  id: number
  username: string
  email?: string
  nickname?: string
  departmentId?: number
  departmentName?: string
}

export const userApi = {
  getCurrentUser: async (): Promise<{ code: number; data: UserInfo }> => {
    return apiClient.get('/auth/me')
  },
}

