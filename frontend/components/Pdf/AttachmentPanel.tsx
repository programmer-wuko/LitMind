'use client'

import { useState, useEffect } from 'react'
import { Upload, Download, Trash2, File, Loader2 } from 'lucide-react'
import { attachmentApi, Attachment } from '@/lib/api/attachment'
import toast from 'react-hot-toast'

interface AttachmentPanelProps {
  fileId: number
}

export function AttachmentPanel({ fileId }: AttachmentPanelProps) {
  const [attachments, setAttachments] = useState<Attachment[]>([])
  const [loading, setLoading] = useState(true)
  const [uploading, setUploading] = useState(false)

  useEffect(() => {
    loadAttachments()
  }, [fileId])

  const loadAttachments = async () => {
    try {
      setLoading(true)
      const res = await attachmentApi.getAttachments(fileId)
      setAttachments(res.data || [])
    } catch (error: any) {
      toast.error('加载附件失败: ' + (error.message || '未知错误'))
    } finally {
      setLoading(false)
    }
  }

  const handleUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    try {
      setUploading(true)
      await attachmentApi.uploadAttachment(fileId, file)
      toast.success('附件上传成功')
      loadAttachments()
    } catch (error: any) {
      toast.error('附件上传失败: ' + (error.message || '未知错误'))
    } finally {
      setUploading(false)
      // 清空input，允许重复上传同一文件
      if (e.target) {
        e.target.value = ''
      }
    }
  }

  const handleDownload = async (attachment: Attachment) => {
    try {
      const url = attachmentApi.getDownloadUrl(attachment.id)
      const token = localStorage.getItem('token')
      
      // 使用fetch下载文件（支持认证）
      const response = await fetch(url, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })
      
      if (!response.ok) {
        throw new Error('下载失败')
      }
      
      // 获取文件blob
      const blob = await response.blob()
      
      // 创建临时链接下载
      const downloadUrl = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = downloadUrl
      link.download = attachment.name
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(downloadUrl)
      
      toast.success('附件下载成功')
    } catch (error: any) {
      toast.error('附件下载失败: ' + (error.message || '未知错误'))
    }
  }

  const handleDelete = async (attachmentId: number) => {
    if (!confirm('确定要删除这个附件吗？')) {
      return
    }

    try {
      await attachmentApi.deleteAttachment(attachmentId)
      toast.success('附件删除成功')
      loadAttachments()
    } catch (error: any) {
      toast.error('附件删除失败: ' + (error.message || '未知错误'))
    }
  }

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return bytes + ' B'
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
    return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
  }

  return (
    <div className="p-6">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-semibold">附件管理</h3>
        <label className="flex items-center space-x-2 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 cursor-pointer disabled:opacity-50">
          {uploading ? (
            <>
              <Loader2 className="h-4 w-4 animate-spin" />
              <span>上传中...</span>
            </>
          ) : (
            <>
              <Upload className="h-4 w-4" />
              <span>上传附件</span>
            </>
          )}
          <input
            type="file"
            className="hidden"
            onChange={handleUpload}
            disabled={uploading}
          />
        </label>
      </div>

      {loading ? (
        <div className="flex items-center justify-center py-8">
          <Loader2 className="h-6 w-6 animate-spin text-gray-400" />
        </div>
      ) : attachments.length === 0 ? (
        <div className="text-center py-8 text-gray-500">
          <File className="h-12 w-12 mx-auto mb-4 opacity-50" />
          <p>暂无附件</p>
        </div>
      ) : (
        <div className="space-y-2">
          {attachments.map((attachment) => (
            <div
              key={attachment.id}
              className="flex items-center justify-between p-4 border rounded-lg hover:bg-gray-50"
            >
              <div className="flex items-center space-x-4 flex-1 min-w-0">
                <File className="h-8 w-8 text-primary-600 flex-shrink-0" />
                <div className="flex-1 min-w-0">
                  <h4 className="font-medium text-gray-900 truncate">
                    {attachment.name}
                  </h4>
                  <p className="text-sm text-gray-500">
                    {formatFileSize(attachment.fileSize)} •{' '}
                    {new Date(attachment.createdAt).toLocaleDateString()}
                  </p>
                </div>
              </div>
              <div className="flex items-center space-x-2">
                <button
                  onClick={() => handleDownload(attachment)}
                  className="p-2 text-primary-600 hover:bg-primary-50 rounded"
                  title="下载"
                >
                  <Download className="h-5 w-5" />
                </button>
                <button
                  onClick={() => handleDelete(attachment.id)}
                  className="p-2 text-red-500 hover:bg-red-50 rounded"
                  title="删除"
                >
                  <Trash2 className="h-5 w-5" />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

