'use client'

import { useState } from 'react'
import { FileItem } from '@/lib/api/file'
import { FileText, Trash2, Eye, Edit2, Check, X, CheckCircle, Loader2 } from 'lucide-react'
import { fileApi } from '@/lib/api/file'
import { pdfApi } from '@/lib/api/pdf'
import toast from 'react-hot-toast'

interface FileListProps {
  files: FileItem[]
  onFileClick: (file: FileItem) => void
  onRefresh: () => void
}

export function FileList({ files, onFileClick, onRefresh }: FileListProps) {
  const [editingId, setEditingId] = useState<number | null>(null)
  const [editingName, setEditingName] = useState('')

  const handleDelete = async (fileId: number, e: React.MouseEvent) => {
    e.stopPropagation()
    const file = files.find((f) => f.id === fileId)
    if (!confirm(`确定要删除文件"${file?.name}"吗？此操作不可恢复。`)) {
      return
    }

    try {
      await fileApi.deleteFile(fileId)
      toast.success('文件删除成功')
      onRefresh()
    } catch (error: any) {
      toast.error('删除文件失败: ' + (error.message || '未知错误'))
    }
  }

  const handleRename = (file: FileItem, e: React.MouseEvent) => {
    e.stopPropagation()
    setEditingId(file.id)
    setEditingName(file.name)
  }

  const handleSaveRename = async (fileId: number) => {
    if (!editingName.trim()) {
      toast.error('文件名不能为空')
      return
    }

    try {
      await fileApi.updateFile(fileId, editingName)
      toast.success('重命名成功')
      setEditingId(null)
      setEditingName('')
      onRefresh()
    } catch (error: any) {
      toast.error('重命名失败: ' + (error.message || '未知错误'))
    }
  }

  const handleCancelRename = () => {
    setEditingId(null)
    setEditingName('')
  }

  const getFileStatus = (file: FileItem) => {
    if (file.uploadStatus === 'PROCESSING' || file.uploadStatus === 'ANALYZING') {
      return { icon: Loader2, text: '处理中', className: 'text-blue-500 animate-spin' }
    }
    if (file.uploadStatus === 'COMPLETED') {
      return { icon: CheckCircle, text: '已分析', className: 'text-green-500' }
    }
    return null
  }

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return bytes + ' B'
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
    return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
  }

  if (files.length === 0) {
    return (
      <div className="text-center py-12 text-gray-500">
        <FileText className="h-12 w-12 mx-auto mb-4 opacity-50" />
        <p>暂无文件</p>
      </div>
    )
  }

  return (
    <div className="space-y-2">
      {files.map((file) => {
        const status = getFileStatus(file)
        const isEditing = editingId === file.id

        return (
          <div
            key={file.id}
            className="flex items-center justify-between p-4 border rounded-lg hover:bg-gray-50"
          >
            <div className="flex items-center space-x-4 flex-1 min-w-0">
              <div className="relative">
                <FileText className="h-8 w-8 text-primary-600" />
                {status && (
                  <status.icon
                    className={`h-4 w-4 absolute -top-1 -right-1 ${status.className}`}
                    title={status.text}
                  />
                )}
              </div>
              <div className="flex-1 min-w-0">
                {isEditing ? (
                  <div className="flex items-center space-x-2">
                    <input
                      type="text"
                      value={editingName}
                      onChange={(e) => setEditingName(e.target.value)}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter') {
                          handleSaveRename(file.id)
                        } else if (e.key === 'Escape') {
                          handleCancelRename()
                        }
                      }}
                      className="flex-1 px-2 py-1 border rounded text-sm"
                      autoFocus
                      onClick={(e) => e.stopPropagation()}
                    />
                    <button
                      onClick={(e) => {
                        e.stopPropagation()
                        handleSaveRename(file.id)
                      }}
                      className="p-1 text-green-600 hover:bg-green-50 rounded"
                    >
                      <Check className="h-4 w-4" />
                    </button>
                    <button
                      onClick={(e) => {
                        e.stopPropagation()
                        handleCancelRename()
                      }}
                      className="p-1 text-gray-600 hover:bg-gray-100 rounded"
                    >
                      <X className="h-4 w-4" />
                    </button>
                  </div>
                ) : (
                  <>
                    <h3
                      className="font-medium text-gray-900 truncate cursor-pointer"
                      onClick={() => onFileClick(file)}
                    >
                      {file.name}
                    </h3>
                    <p className="text-sm text-gray-500">
                      {formatFileSize(file.fileSize)} • {new Date(file.createdAt).toLocaleDateString()}
                    </p>
                  </>
                )}
              </div>
            </div>
            {!isEditing && (
              <div className="flex items-center space-x-2">
                {file.fileType === 'application/pdf' && (
                  <button
                    onClick={(e) => {
                      e.stopPropagation()
                      onFileClick(file)
                    }}
                    className="p-2 text-primary-600 hover:bg-primary-50 rounded"
                    title="查看"
                  >
                    <Eye className="h-5 w-5" />
                  </button>
                )}
                <button
                  onClick={(e) => handleRename(file, e)}
                  className="p-2 text-gray-600 hover:bg-gray-100 rounded"
                  title="重命名"
                >
                  <Edit2 className="h-5 w-5" />
                </button>
                <button
                  onClick={(e) => handleDelete(file.id, e)}
                  className="p-2 text-red-500 hover:bg-red-50 rounded"
                  title="删除"
                >
                  <Trash2 className="h-5 w-5" />
                </button>
              </div>
            )}
          </div>
        )
      })}
    </div>
  )
}

