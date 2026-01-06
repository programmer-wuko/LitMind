'use client'

import { useState, useMemo, useEffect } from 'react'
import { Folder, Plus, Trash2, Edit2, Check, X, ChevronRight, ChevronDown } from 'lucide-react'
import { fileApi, Folder as FolderType } from '@/lib/api/file'
import toast from 'react-hot-toast'

interface FolderTreeProps {
  folders: FolderType[]
  selectedFolderId?: number
  onSelectFolder: (folderId: number | undefined) => void
  onRefresh: () => void
  defaultIsPublic?: boolean // 默认文件夹可见性：true=文献管理，false=我的文献
}

interface FolderNode extends FolderType {
  children: FolderNode[]
}

export function FolderTree({
  folders,
  selectedFolderId,
  onSelectFolder,
  onRefresh,
  defaultIsPublic = false,
}: FolderTreeProps) {
  const [newFolderName, setNewFolderName] = useState('')
  const [showNewFolder, setShowNewFolder] = useState(false)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [editingName, setEditingName] = useState('')
  const [expandedFolders, setExpandedFolders] = useState<Set<number>>(new Set())

  // 将扁平列表转换为树形结构
  const folderTree = useMemo(() => {
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
          // 如果父文件夹不存在，也作为根文件夹处理
          rootFolders.push(node)
        }
      }
    })

    // 对每个层级的子文件夹进行排序（按名称）
    const sortChildren = (nodes: FolderNode[]) => {
      nodes.sort((a, b) => a.name.localeCompare(b.name))
      nodes.forEach((node) => {
        if (node.children.length > 0) {
          sortChildren(node.children)
        }
      })
    }
    sortChildren(rootFolders)

    // 调试日志：打印树形结构
    if (process.env.NODE_ENV === 'development') {
      console.log('文件夹树形结构:', rootFolders)
      console.log('所有文件夹:', folders.map(f => `ID:${f.id}, 名称:${f.name}, 父ID:${f.parentId}`))
    }

    return rootFolders
  }, [folders])

  // 当选中文件夹时，自动展开到该文件夹的路径
  useEffect(() => {
    if (selectedFolderId !== undefined) {
      // 找到选中文件夹的所有父文件夹
      const findPath = (targetId: number, nodes: FolderNode[]): number[] => {
        for (const node of nodes) {
          if (node.id === targetId) {
            return [node.id]
          }
          const childPath = findPath(targetId, node.children)
          if (childPath.length > 0) {
            return [node.id, ...childPath]
          }
        }
        return []
      }

      const path = findPath(selectedFolderId, folderTree)
      // 展开路径上的所有文件夹（除了最后一个，因为它是选中的文件夹）
      if (path.length > 1) {
        setExpandedFolders((prev) => {
          const next = new Set(prev)
          // 展开路径上除了最后一个的所有文件夹
          for (let i = 0; i < path.length - 1; i++) {
            next.add(path[i])
          }
          return next
        })
      }
    }
  }, [selectedFolderId, folderTree])

  // 当文件夹数据更新时，如果有选中的父文件夹，自动展开它（用于显示新创建的子文件夹）
  useEffect(() => {
    if (selectedFolderId !== undefined && folderTree.length > 0) {
      // 检查选中的文件夹是否有子文件夹
      const findNode = (targetId: number, nodes: FolderNode[]): FolderNode | null => {
        for (const node of nodes) {
          if (node.id === targetId) {
            return node
          }
          const found = findNode(targetId, node.children)
          if (found) {
            return found
          }
        }
        return null
      }

      const node = findNode(selectedFolderId, folderTree)
      // 如果选中的文件夹有子文件夹，自动展开它
      if (node && node.children.length > 0) {
        setExpandedFolders((prev) => {
          const next = new Set(prev)
          next.add(selectedFolderId)
          // 调试日志
          if (process.env.NODE_ENV === 'development') {
            console.log('自动展开文件夹:', selectedFolderId, '子文件夹数量:', node.children.length, '子文件夹:', node.children.map(c => c.name))
          }
          return next
        })
      }
    }
  }, [folders, selectedFolderId, folderTree])

  const handleCreateFolder = async () => {
    if (!newFolderName.trim()) {
      toast.error('请输入文件夹名称')
      return
    }

    try {
      await fileApi.createFolder(newFolderName, selectedFolderId, defaultIsPublic)
      toast.success('文件夹创建成功')
      setNewFolderName('')
      setShowNewFolder(false)
      
      // 刷新数据，useEffect 会自动展开父文件夹
      onRefresh()
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || error.message || '未知错误'
      toast.error('创建文件夹失败: ' + errorMessage)
    }
  }

  const handleDeleteFolder = async (folderId: number, e: React.MouseEvent) => {
    e.stopPropagation()
    const folder = folders.find((f) => f.id === folderId)
    if (!confirm(`确定要删除文件夹"${folder?.name}"吗？此操作将删除文件夹及其所有内容，且不可恢复。`)) {
      return
    }

    try {
      await fileApi.deleteFolder(folderId)
      toast.success('文件夹删除成功')
      if (selectedFolderId === folderId) {
        onSelectFolder(undefined)
      }
      onRefresh()
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || error.message || '未知错误'
      toast.error('删除文件夹失败: ' + errorMessage)
    }
  }

  const handleRename = (folder: FolderType, e: React.MouseEvent) => {
    e.stopPropagation()
    setEditingId(folder.id)
    setEditingName(folder.name)
  }

  const handleSaveRename = async (folderId: number) => {
    if (!editingName.trim()) {
      toast.error('文件夹名称不能为空')
      return
    }

    try {
      await fileApi.updateFolder(folderId, editingName)
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

  const toggleExpand = (folderId: number, e: React.MouseEvent) => {
    e.stopPropagation()
    setExpandedFolders((prev) => {
      const next = new Set(prev)
      if (next.has(folderId)) {
        next.delete(folderId)
      } else {
        next.add(folderId)
      }
      return next
    })
  }

  // 递归渲染文件夹节点
  const renderFolderNode = (node: FolderNode, level: number = 0) => {
    const isEditing = editingId === node.id
    const isExpanded = expandedFolders.has(node.id)
    const hasChildren = node.children.length > 0
    
    // 调试日志
    if (process.env.NODE_ENV === 'development' && hasChildren) {
      console.log(`文件夹 ${node.name} (ID: ${node.id}): 展开=${isExpanded}, 子文件夹数=${node.children.length}, 子文件夹:`, node.children.map(c => `${c.name}(${c.id})`))
    }

    return (
      <div key={node.id}>
        <div
          className={`flex items-center justify-between px-3 py-2 rounded ${
            selectedFolderId === node.id
              ? 'bg-primary-100 text-primary-700'
              : 'hover:bg-gray-100'
          } ${isEditing ? '' : 'cursor-pointer'}`}
          style={{ paddingLeft: `${12 + level * 20}px` }}
          onClick={() => !isEditing && onSelectFolder(node.id)}
        >
          <div className="flex items-center space-x-2 flex-1 min-w-0">
            {isEditing ? (
              <div className="flex items-center space-x-2 flex-1">
                <input
                  type="text"
                  value={editingName}
                  onChange={(e) => setEditingName(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') {
                      handleSaveRename(node.id)
                    } else if (e.key === 'Escape') {
                      handleCancelRename()
                    }
                  }}
                  className="flex-1 px-2 py-1 text-sm border rounded"
                  autoFocus
                  onClick={(e) => e.stopPropagation()}
                />
                <button
                  onClick={(e) => {
                    e.stopPropagation()
                    handleSaveRename(node.id)
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
                {hasChildren ? (
                  <button
                    onClick={(e) => toggleExpand(node.id, e)}
                    className="p-0.5 hover:bg-gray-200 rounded"
                  >
                    {isExpanded ? (
                      <ChevronDown className="h-4 w-4" />
                    ) : (
                      <ChevronRight className="h-4 w-4" />
                    )}
                  </button>
                ) : (
                  <div className="w-5" /> // 占位符，保持对齐
                )}
                <Folder className="h-4 w-4 flex-shrink-0" />
                <span className="truncate">{node.name}</span>
              </>
            )}
          </div>
          {!isEditing && (
            <div className="flex items-center space-x-1">
              <button
                onClick={(e) => handleRename(node, e)}
                className="p-1 text-gray-600 hover:text-gray-800 hover:bg-gray-200 rounded"
                title="重命名"
              >
                <Edit2 className="h-4 w-4" />
              </button>
              <button
                onClick={(e) => handleDeleteFolder(node.id, e)}
                className="p-1 text-red-500 hover:text-red-700 hover:bg-red-50 rounded"
                title="删除"
              >
                <Trash2 className="h-4 w-4" />
              </button>
            </div>
          )}
        </div>
        {hasChildren && isExpanded && (
          <div className="ml-0">
            {node.children.map((child) => renderFolderNode(child, level + 1))}
          </div>
        )}
      </div>
    )
  }

  return (
    <div className="space-y-2">
      <button
        onClick={() => onSelectFolder(undefined)}
        className={`w-full text-left px-3 py-2 rounded flex items-center space-x-2 ${
          selectedFolderId === undefined
            ? 'bg-primary-100 text-primary-700'
            : 'hover:bg-gray-100'
        }`}
      >
        <Folder className="h-4 w-4" />
        <span>根目录</span>
      </button>

      {folderTree.map((node) => renderFolderNode(node))}

      <div className="pt-2 border-t">
        {showNewFolder ? (
          <div className="space-y-2">
            <input
              type="text"
              value={newFolderName}
              onChange={(e) => setNewFolderName(e.target.value)}
              placeholder="文件夹名称"
              className="w-full px-2 py-1 text-sm border rounded"
              autoFocus
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  handleCreateFolder()
                } else if (e.key === 'Escape') {
                  setShowNewFolder(false)
                  setNewFolderName('')
                }
              }}
            />
            <div className="flex space-x-2">
              <button
                onClick={handleCreateFolder}
                className="flex-1 px-2 py-1 text-sm bg-primary-600 text-white rounded hover:bg-primary-700"
              >
                创建
              </button>
              <button
                onClick={() => {
                  setShowNewFolder(false)
                  setNewFolderName('')
                }}
                className="flex-1 px-2 py-1 text-sm bg-gray-200 rounded hover:bg-gray-300"
              >
                取消
              </button>
            </div>
          </div>
        ) : (
          <button
            onClick={() => setShowNewFolder(true)}
            className="w-full flex items-center space-x-2 px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 rounded"
          >
            <Plus className="h-4 w-4" />
            <span>新建文件夹</span>
          </button>
        )}
      </div>
    </div>
  )
}

