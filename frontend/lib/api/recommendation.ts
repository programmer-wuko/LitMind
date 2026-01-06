import { apiClient } from './client'

export interface Recommendation {
  id: number
  userId: number
  recommendedFileId: number | null
  externalPaperId: string | null
  paperTitle: string | null
  paperAuthors: string | null
  paperSource: string | null
  paperUrl: string | null
  recommendationReason: string | null
  recommendationScore: number | null
  feedback: string | null
  createdAt: string
}

export const recommendationApi = {
  getRecommendations: async (): Promise<{ code: number; data: Recommendation[] }> => {
    return apiClient.get('/recommendations')
  },

  generateRecommendations: async (): Promise<{ code: number }> => {
    return apiClient.post('/recommendations/generate')
  },

  updateFeedback: async (id: number, feedback: string): Promise<{ code: number }> => {
    return apiClient.put(`/recommendations/${id}/feedback`, { feedback })
  },
}

