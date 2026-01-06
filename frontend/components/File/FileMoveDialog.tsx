'use client'

import { useState, useEffect } from 'react'
import { Folder, X } from 'lucide-react'
import { fileApi, Folder as FolderType } from '@/lib/api/file'
import toast from 'react-hot-toast'

interface FileMoveDialogProps {
  isOpen: boolean
  onClose: () => void
  onConfirm: (targetFolderId: number | null) => Promise<void>
  currentFolderId?: number | null
  isPublic?: boolean
}

interface FolderNode extends FolderType {
  children: FolderNode[]
}

export function FileMoveDialog({
  isOpen,
  onClose,
  onConfirm,
  currentFolderId,
  isPublic = false,
}: FileMoveDialogProps) {
  const [folders, setFolders] = useState<FolderType[]>([])
  const [selectedFolderId, setSelectedFolderId] = useState<number | null | undefined>(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (isOpen) {
      loadFolders()
      setSelectedFolderId(null) // 重置选择
    }
  }, [isOpen, isPublic])

  const loadFolders = async () => {
    try {
      setLoading(true)
      const response = await fileApi.getFolders(undefined, isPublic)
      setFolders(response.data || [])
    } catch (error: any) {
      console.error('加载文件夹失败:', error)
      toast.error('加载文件夹失败')
    } finally {
      setLoading(false)
    }
  }

  // 构建文件夹树
  const buildFolderTree = (folders: FolderType[]): FolderNode[] => {
    const folderMap = new Map<number, FolderNode>()
    const rootFolders: FolderNode[] = []

    // 创建所有文件夹节点
    folders.forEach((folder) => {
      folderMap.set(folder.id, { ...folder, children: [] })
    })

    // 构建树形结构
    folders.forEach((folder) => {
      const node = folderMap.get(folder.id)!
      if (folder.parentId === null || folder.parentId === undefined) {
        rootFolders.push(node)
      } else {
        const parent = folderMap.get(folder.parentId)
        if (parent) {
          parent.children.push(node)
        } else {
          rootFolders.push(node)
        }
      }
    })

    return rootFolders
  }

  const folderTree = buildFolderTree(folders)

  const handleConfirm = async () => {
    try {
      await onConfirm(selectedFolderId === undefined ? null : selectedFolderId)
      onClose()
    } catch (error) {
      // 错误已在onConfirm中处理
    }
  }

  const renderFolderNode = (node: FolderNode, level: number = 0) => {
    const isSelected = selectedFolderId === node.id
    const isCurrentFolder = currentFolderId === node.id

    return (
      <div key={node.id}>
        <div
          className={`flex items-center px-3 py-2 rounded cursor-pointer ${
            isSelected
              ? 'bg-primary-100 text-primary-700'
              : isCurrentFolder
              ? 'bg-gray-100 text-gray-500 cursor-not-allowed'
              : 'hover:bg-gray-50'
          }`}
          style={{ paddingLeft: `${12 + level * 20}px` }}
          onClick={() => !isCurrentFolder && setSelectedFolderId(node.id)}
        >
          <Folder className="h-4 w-4 mr-2 flex-shrink-0" />
          <span className="truncate">
            {node.name}
            {isCurrentFolder && <span className="text-xs ml-2">(当前位置)</span>}
          </span>
        </div>
        {node.children.length > 0 && (
          <div className="ml-0">
            {node.children.map((child) => renderFolderNode(child, level + 1))}
          </div>
        )}
      </div>
    )
  }

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 bg-gray-600 bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-md max-h-[80vh] flex flex-col">
        <div className="flex justify-between items-center p-6 border-b">
          <h3 className="text-lg font-semibold">移动到文件夹</h3>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto p-4">
          {loading ? (
            <div className="text-center py-8 text-gray-500">加载中...</div>
          ) : (
            <div className="space-y-1">
              <div
                className={`flex items-center px-3 py-2 rounded cursor-pointer ${
                  selectedFolderId === null
                    ? 'bg-primary-100 text-primary-700'
                    : 'hover:bg-gray-50'
                }`}
                onClick={() => setSelectedFolderId(null)}
              >
                <Folder className="h-4 w-4 mr-2" />
                <span>根目录</span>
              </div>
              {folderTree.map((node) => renderFolderNode(node))}
            </div>
          )}
        </div>

        <div className="flex justify-end space-x-2 p-6 border-t">
          <button
            onClick={onClose}
            className="px-4 py-2 text-gray-700 bg-gray-200 rounded-lg hover:bg-gray-300"
          >
            取消
          </button>
          <button
            onClick={handleConfirm}
            disabled={loading}
            className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            确定
          </button>
        </div>
      </div>
    </div>
  )
}

