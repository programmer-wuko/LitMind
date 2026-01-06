'use client'

import { useEffect, useState, useMemo } from 'react'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/lib/store/auth'
import { fileApi, FileItem, Folder } from '@/lib/api/file'
import DashboardLayout from '@/components/Layout/DashboardLayout'
import { FileMoveDialog } from '@/components/File/FileMoveDialog'
import { Eye, Edit2, Trash2, Tag, Move } from 'lucide-react'
import toast from 'react-hot-toast'

export default function MyDocumentsPage() {
  const router = useRouter()
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
  const [files, setFiles] = useState<FileItem[]>([])
  const [folders, setFolders] = useState<Folder[]>([])
  const [loading, setLoading] = useState(true)
  const [searchKeyword, setSearchKeyword] = useState('')
  const [editingId, setEditingId] = useState<number | null>(null)
  const [editingName, setEditingName] = useState('')
  const [moveDialogOpen, setMoveDialogOpen] = useState(false)
  const [fileToMove, setFileToMove] = useState<FileItem | null>(null)

  useEffect(() => {
    if (!isAuthenticated()) {
      router.push('/login')
      return
    }
    loadData()
  }, [])

  const loadData = async () => {
    await Promise.all([loadFiles(), loadFolders()])
  }

  const loadFiles = async () => {
    try {
      setLoading(true)
      // 我的文献：获取用户的所有文件（不区分文件夹，只显示个人上传的文件）
      const filesRes = await fileApi.getFiles(undefined, false)
      setFiles(filesRes.data || [])
    } catch (error: any) {
      toast.error('加载文件失败: ' + (error.message || '未知错误'))
    } finally {
      setLoading(false)
    }
  }

  const loadFolders = async () => {
    try {
      // 加载所有文件夹用于构建路径
      const foldersRes = await fileApi.getFolders(undefined, false)
      setFolders(foldersRes.data || [])
    } catch (error: any) {
      console.warn('加载文件夹失败:', error)
    }
  }

  const handleFileClick = (file: FileItem) => {
    if (file.fileType === 'application/pdf') {
      router.push(`/pdf/${file.id}`)
    } else {
      toast('暂不支持预览此类型文件')
    }
  }

  const handleDelete = async (fileId: number) => {
    const file = files.find((f) => f.id === fileId)
    if (!confirm(`确定要删除文件"${file?.name}"吗？此操作不可恢复。`)) {
      return
    }

    try {
      await fileApi.deleteFile(fileId)
      toast.success('文件删除成功')
      loadFiles()
    } catch (error: any) {
      toast.error('删除文件失败: ' + (error.message || '未知错误'))
    }
  }

  const handleRename = (file: FileItem) => {
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
      loadFiles()
    } catch (error: any) {
      toast.error('重命名失败: ' + (error.message || '未知错误'))
    }
  }

  const handleCancelRename = () => {
    setEditingId(null)
    setEditingName('')
  }

  const handleMove = (file: FileItem) => {
    setFileToMove(file)
    setMoveDialogOpen(true)
  }

  const handleMoveConfirm = async (targetFolderId: number | null) => {
    if (!fileToMove) return

    try {
      await fileApi.moveFile(fileToMove.id, targetFolderId)
      toast.success('文件移动成功')
      // 重新加载文件列表和文件夹列表（用于更新路径显示）
      await Promise.all([loadFiles(), loadFolders()])
      setMoveDialogOpen(false)
      setFileToMove(null)
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || error.message || '未知错误'
      toast.error('移动文件失败: ' + errorMessage)
    }
  }

  const handleSearch = () => {
    // 搜索功能已在过滤中实现
  }

  const handleReset = () => {
    setSearchKeyword('')
    loadFiles()
  }

  // 构建文件夹路径
  const getFolderPath = (folderId: number | null): string => {
    if (!folderId) return '根目录'
    
    const folderMap = new Map<number, Folder>()
    folders.forEach((f) => folderMap.set(f.id, f))

    const buildPath = (id: number, visited: Set<number> = new Set()): string => {
      // 防止循环引用
      if (visited.has(id)) {
        return '循环引用'
      }
      visited.add(id)
      
      const folder = folderMap.get(id)
      if (!folder) {
        // 如果文件夹不在列表中，可能是文件夹列表未加载完整，返回ID
        return `文件夹 #${id}`
      }
      
      if (folder.parentId === null || folder.parentId === undefined) {
        return folder.name
      }
      
      return `${buildPath(folder.parentId, visited)} / ${folder.name}`
    }

    try {
      return buildPath(folderId)
    } catch (error) {
      console.error('构建文件夹路径失败:', error)
      return `文件夹 #${folderId}`
    }
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('zh-CN')
  }

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return bytes + ' B'
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
    return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
  }

  // 过滤文件列表
  const filteredFiles = searchKeyword
    ? files.filter((file) =>
        file.name.toLowerCase().includes(searchKeyword.toLowerCase())
      )
    : files

  return (
    <DashboardLayout>
      <div className="space-y-6">
        {/* 搜索筛选区域 */}
        <div className="bg-white rounded-lg shadow p-4">
          <div className="flex items-center space-x-4">
            <div className="flex-1">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                各类条件
              </label>
              <input
                type="text"
                value={searchKeyword}
                onChange={(e) => setSearchKeyword(e.target.value)}
                placeholder="请输入搜索关键词..."
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    handleSearch()
                  }
                }}
              />
            </div>
            <div className="flex items-end space-x-2">
              <button
                onClick={handleSearch}
                className="px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
              >
                搜索
              </button>
              <button
                onClick={handleReset}
                className="px-6 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors"
              >
                重置
              </button>
            </div>
          </div>
        </div>

        {/* 文件列表表格 */}
        <div className="bg-white rounded-lg shadow">
          <div className="p-6 border-b">
            <div className="flex justify-between items-center">
              <h2 className="text-lg font-semibold">我的文件</h2>
              <p className="text-sm text-gray-500">
                提示：文件上传请在"文献管理"中进行
              </p>
            </div>
          </div>
          {loading ? (
            <div className="text-center py-12 text-gray-500">加载中...</div>
          ) : filteredFiles.length === 0 ? (
            <div className="text-center py-12 text-gray-500">
              <p>暂无文件</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      序号
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      文件名称
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      文件位置
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      文件大小
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      文件类型
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      创建时间
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      操作
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {filteredFiles.map((file, index) => {
                    const isEditing = editingId === file.id
                    return (
                      <tr key={file.id} className="hover:bg-gray-50">
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {index + 1}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
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
                                className="px-2 py-1 border rounded text-sm"
                                autoFocus
                                onClick={(e) => e.stopPropagation()}
                              />
                              <button
                                onClick={() => handleSaveRename(file.id)}
                                className="text-green-600 hover:text-green-800"
                                title="保存"
                              >
                                ✓
                              </button>
                              <button
                                onClick={handleCancelRename}
                                className="text-gray-600 hover:text-gray-800"
                                title="取消"
                              >
                                ✕
                              </button>
                            </div>
                          ) : (
                            <button
                              onClick={() => handleFileClick(file)}
                              className="text-sm font-medium text-primary-600 hover:text-primary-800"
                            >
                              {file.name}
                            </button>
                          )}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {getFolderPath(file.folderId)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {formatFileSize(file.fileSize)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                            <Tag className="h-3 w-3 mr-1" />
                            {file.fileType?.split('/')[1] || '未知'}
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {formatDate(file.createdAt)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                          <div className="flex items-center space-x-2">
                            {file.fileType === 'application/pdf' && (
                              <button
                                onClick={() => handleFileClick(file)}
                                className="text-primary-600 hover:text-primary-900"
                                title="查看"
                              >
                                <Eye className="h-4 w-4" />
                              </button>
                            )}
                            <button
                              onClick={() => handleMove(file)}
                              className="text-blue-600 hover:text-blue-900"
                              title="移动"
                            >
                              <Move className="h-4 w-4" />
                            </button>
                            <button
                              onClick={() => handleRename(file)}
                              className="text-gray-600 hover:text-gray-900"
                              title="重命名"
                            >
                              <Edit2 className="h-4 w-4" />
                            </button>
                            <button
                              onClick={() => handleDelete(file.id)}
                              className="text-red-600 hover:text-red-900"
                              title="删除"
                            >
                              <Trash2 className="h-4 w-4" />
                            </button>
                          </div>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* 文件移动对话框 */}
        <FileMoveDialog
          isOpen={moveDialogOpen}
          onClose={() => {
            setMoveDialogOpen(false)
            setFileToMove(null)
          }}
          onConfirm={handleMoveConfirm}
          currentFolderId={fileToMove?.folderId}
          isPublic={false}
        />
      </div>
    </DashboardLayout>
  )
}

