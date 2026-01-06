import { apiClient } from './client'

export interface FileItem {
  id: number
  name: string
  originalName: string
  fileSize: number
  fileType: string
  folderId: number | null
  isPublic?: boolean
  createdAt: string
  uploadStatus?: string // 可选字段，可能为: PROCESSING, ANALYZING, COMPLETED
}

export interface Folder {
  id: number
  name: string
  parentId: number | null
  path: string
  userId: number
  isPublic?: boolean
  createdAt: string
  updatedAt: string
}

export const fileApi = {
  getFiles: async (folderId?: number, isPublic?: boolean): Promise<{ code: number; data: FileItem[] }> => {
    const params: any = {}
    if (folderId !== undefined) {
      params.folderId = folderId
    }
    if (isPublic !== undefined) {
      params.isPublic = isPublic
    }
    return apiClient.get('/files', { params })
  },

  uploadFile: async (file: File, folderId?: number, isPublic?: boolean): Promise<{ code: number; data: FileItem }> => {
    const formData = new FormData()
    formData.append('file', file)
    if (folderId) {
      formData.append('folderId', folderId.toString())
    }
    if (isPublic !== undefined) {
      formData.append('isPublic', isPublic.toString())
    }
    // 不要手动设置Content-Type，让axios自动处理（包含boundary）
    return apiClient.post('/files/upload', formData)
  },

  deleteFile: async (fileId: number): Promise<{ code: number }> => {
    return apiClient.delete(`/files/${fileId}`)
  },

  getFolders: async (parentId?: number, isPublic?: boolean): Promise<{ code: number; data: Folder[] }> => {
    const params: any = {}
    if (parentId !== undefined) {
      params.parentId = parentId
    }
    if (isPublic !== undefined) {
      params.isPublic = isPublic
    }
    return apiClient.get('/folders', { params })
  },

  createFolder: async (name: string, parentId?: number, isPublic?: boolean): Promise<{ code: number; data: Folder }> => {
    return apiClient.post('/folders', { name, parentId, isPublic })
  },

  deleteFolder: async (folderId: number): Promise<{ code: number }> => {
    return apiClient.delete(`/folders/${folderId}`)
  },

  updateFolder: async (folderId: number, name: string): Promise<{ code: number; data: Folder }> => {
    return apiClient.put(`/folders/${folderId}`, { name })
  },

  updateFile: async (fileId: number, name?: string, folderId?: number): Promise<{ code: number; data: FileItem }> => {
    return apiClient.put(`/files/${fileId}`, { name, folderId })
  },

  moveFile: async (fileId: number, targetFolderId: number | null): Promise<{ code: number; data: FileItem }> => {
    return apiClient.put(`/files/${fileId}`, { folderId: targetFolderId })
  },
}

