import { apiClient } from './client'

export interface Attachment {
  id: number
  fileId: number
  name: string
  filePath: string
  fileSize: number
  fileType: string
  createdAt: string
}

export const attachmentApi = {
  getAttachments: async (fileId: number): Promise<{ code: number; data: Attachment[] }> => {
    return apiClient.get(`/attachments?fileId=${fileId}`)
  },

  uploadAttachment: async (fileId: number, file: File): Promise<{ code: number; data: Attachment }> => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('fileId', fileId.toString())
    return apiClient.post('/attachments/upload', formData)
  },

  deleteAttachment: async (attachmentId: number): Promise<{ code: number }> => {
    return apiClient.delete(`/attachments/${attachmentId}`)
  },

  getDownloadUrl: (attachmentId: number): string => {
    const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api'
    return `${apiUrl}/attachments/${attachmentId}/download`
  },
}

