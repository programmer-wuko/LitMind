import { apiClient } from './client'

export interface QaRequest {
  fileId: number
  question: string
}

export const aiApi = {
  askQuestion: async (request: QaRequest): Promise<{ code: number; data: string }> => {
    return apiClient.post('/ai/qa', request)
  },
}

