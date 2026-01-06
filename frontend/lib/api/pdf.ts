import { apiClient } from './client'

export interface PdfAnalysis {
  id: number
  fileId: number
  researchBackground: string | null
  coreContent: string | null
  experimentResults: string | null
  additionalInfo: string | null
  analysisStatus: string
  analysisModel: string | null
}

export const pdfApi = {
  getAnalysis: async (fileId: number): Promise<{ code: number; data: PdfAnalysis }> => {
    return apiClient.get(`/pdf/${fileId}/analysis`)
  },

  analyzePdf: async (fileId: number): Promise<{ code: number; data: PdfAnalysis }> => {
    return apiClient.post(`/pdf/${fileId}/analyze`)
  },

  updateAnalysis: async (
    fileId: number,
    analysis: Partial<PdfAnalysis>
  ): Promise<{ code: number; data: PdfAnalysis }> => {
    return apiClient.put(`/pdf/${fileId}/analysis`, analysis)
  },
}

