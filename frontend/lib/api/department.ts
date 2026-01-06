import { apiClient } from './client'

export interface Department {
  id: number
  name: string
  description?: string
  createdAt: string
  updatedAt: string
}

export const departmentApi = {
  getAllDepartments: async (): Promise<{ code: number; data: Department[] }> => {
    return apiClient.get('/departments')
  },

  getDepartmentById: async (id: number): Promise<{ code: number; data: Department }> => {
    return apiClient.get(`/departments/${id}`)
  },

  createDepartment: async (name: string, description?: string): Promise<{ code: number; data: Department }> => {
    return apiClient.post('/departments', { name, description })
  },
}

