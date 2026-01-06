'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/lib/store/auth'
import { fileApi, FileItem, Folder } from '@/lib/api/file'
import { recommendationApi, Recommendation } from '@/lib/api/recommendation'
import DashboardLayout from '@/components/Layout/DashboardLayout'
import { EnhancedRecommendationList } from '@/components/Recommendation/EnhancedRecommendationList'
import { FileUpload, UploadLocation } from '@/components/File/FileUpload'
import { FolderTree } from '@/components/File/FolderTree'
import { FileMoveDialog } from '@/components/File/FileMoveDialog'
import { Eye, Edit2, Trash2, Download, Tag, Move } from 'lucide-react'
import toast from 'react-hot-toast'

export default function DashboardPage() {
  const router = useRouter()
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
  const username = useAuthStore((state) => state.username)
  const [files, setFiles] = useState<FileItem[]>([])
  const [folders, setFolders] = useState<Folder[]>([])
  const [recommendations, setRecommendations] = useState<Recommendation[]>([])
  const [loading, setLoading] = useState(true)
  const [recommendationsLoading, setRecommendationsLoading] = useState(true)
  const [searchKeyword, setSearchKeyword] = useState('')
  const [selectedFolderId, setSelectedFolderId] = useState<number | undefined>(undefined)
  const [moveDialogOpen, setMoveDialogOpen] = useState(false)
  const [fileToMove, setFileToMove] = useState<FileItem | null>(null)
  const [renamingFile, setRenamingFile] = useState<FileItem | null>(null)
  const [newFileName, setNewFileName] = useState('')

  useEffect(() => {
    if (!isAuthenticated()) {
      router.push('/login')
      return
    }
    loadData()
    loadRecommendations()
  }, [])

  useEffect(() => {
    if (isAuthenticated()) {
      loadFiles()
    }
  }, [selectedFolderId])

  const loadData = async () => {
    await Promise.all([loadFolders(), loadFiles()])
  }

  const loadFolders = async () => {
    try {
      // 文献管理：获取公共文件夹（所有人可见）
      const foldersRes = await fileApi.getFolders(undefined, true)
      setFolders(foldersRes.data || [])
    } catch (error: any) {
      toast.error('加载文件夹失败: ' + (error.message || '未知错误'))
    }
  }

  const loadFiles = async () => {
    try {
      setLoading(true)
      // 文献管理：获取公共文件（所有人可见）
      const filesRes = await fileApi.getFiles(selectedFolderId, true)
      setFiles(filesRes.data || [])
    } catch (error: any) {
      toast.error('加载文件失败: ' + (error.message || '未知错误'))
    } finally {
      setLoading(false)
    }
  }

  const loadRecommendations = async () => {
    try {
      setRecommendationsLoading(true)
      const res = await recommendationApi.getRecommendations()
      setRecommendations(res.data || [])
    } catch (error: any) {
      console.warn('加载推荐失败:', error)
    } finally {
      setRecommendationsLoading(false)
    }
  }

  const handleFileUpload = async (file: File, isPublic: boolean) => {
    try {
      await fileApi.uploadFile(file, selectedFolderId, isPublic)
      toast.success('文件上传成功')
      loadFiles()
      loadFolders()
    } catch (error: any) {
      toast.error('文件上传失败: ' + (error.message || '未知错误'))
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
    if (!confirm('确定要删除这个文件吗？')) {
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

  const handleRename = (file: FileItem) => {
    setRenamingFile(file)
    setNewFileName(file.name)
  }

  const handleSaveRename = async () => {
    if (!renamingFile || !newFileName.trim()) return

    try {
      await fileApi.updateFile(renamingFile.id, newFileName.trim(), renamingFile.folderId ?? undefined)
      toast.success('文件重命名成功')
      loadFiles()
      setRenamingFile(null)
      setNewFileName('')
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || error.message || '未知错误'
      toast.error('重命名文件失败: ' + errorMessage)
    }
  }

  const handleSearch = () => {
    // 搜索功能已在过滤中实现
    // 搜索会自动触发，无需额外提示
  }

  const handleReset = () => {
    setSearchKeyword('')
    setSelectedFolderId(undefined)
    loadFiles()
  }

  const getFolderPath = (folderId: number | null): string => {
    if (!folderId) return '根目录'
    const folder = folders.find((f) => f.id === folderId)
    return folder ? folder.name : '未知文件夹'
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
        {/* 推荐模块 - 增强版 */}
        <div className="bg-white rounded-lg shadow p-6">
          <EnhancedRecommendationList
            recommendations={recommendations}
            onRefresh={loadRecommendations}
            onImport={async (id) => {
              toast('导入功能开发中...')
            }}
            onNotInterested={(id) => {
              setRecommendations((prev) => prev.filter((r) => r.id !== id))
            }}
          />
        </div>

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
                placeholder="请输入文件名称、关键词..."
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

        {/* 文献管理区域 - 文件夹树 + 文件列表 */}
        <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
          {/* 左侧：文件夹树 */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-lg shadow p-4">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-lg font-semibold">文件夹</h2>
                <FileUpload onUpload={handleFileUpload} defaultLocation="public" />
              </div>
              <FolderTree
                folders={folders}
                selectedFolderId={selectedFolderId}
                onSelectFolder={setSelectedFolderId}
                onRefresh={loadFolders}
                defaultIsPublic={true}
              />
            </div>
          </div>

          {/* 右侧：文件列表表格 */}
          <div className="lg:col-span-3">
            <div className="bg-white rounded-lg shadow">
              <div className="p-6 border-b">
                <h2 className="text-lg font-semibold">文件列表</h2>
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
                          标签
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          创建时间
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          修改时间
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          创建人
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          操作
                        </th>
                      </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                      {filteredFiles.map((file, index) => (
                        <tr key={file.id} className="hover:bg-gray-50">
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {index + 1}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            {renamingFile?.id === file.id ? (
                              <div className="flex items-center space-x-2">
                                <input
                                  type="text"
                                  value={newFileName}
                                  onChange={(e) => setNewFileName(e.target.value)}
                                  onBlur={handleSaveRename}
                                  onKeyPress={(e) => e.key === 'Enter' && handleSaveRename()}
                                  className="px-2 py-1 border border-primary-300 rounded focus:outline-none focus:ring-2 focus:ring-primary-500"
                                  autoFocus
                                />
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
                          <td className="px-6 py-4 whitespace-nowrap">
                            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                              <Tag className="h-3 w-3 mr-1" />
                              {file.fileType?.split('/')[1] || '未知'}
                            </span>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {formatDate(file.createdAt)}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {formatDate(file.createdAt)}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {username || '未知'}
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
                            onClick={() => handleRename(file)}
                            className="text-yellow-600 hover:text-yellow-900"
                            title="重命名"
                          >
                            <Edit2 className="h-4 w-4" />
                          </button>
                          <button
                            onClick={() => handleMove(file)}
                            className="text-blue-600 hover:text-blue-900"
                            title="移动"
                          >
                            <Move className="h-4 w-4" />
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
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>
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
          isPublic={true}
        />
      </div>
    </DashboardLayout>
  )
}
