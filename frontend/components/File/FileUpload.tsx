'use client'

import { useRef, useState } from 'react'
import { Upload, X } from 'lucide-react'

export type UploadLocation = 'my' | 'public' | 'both'

interface FileUploadProps {
  onUpload: (file: File, isPublic: boolean) => void
  defaultLocation?: UploadLocation
}

export function FileUpload({ onUpload, defaultLocation = 'my' }: FileUploadProps) {
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [uploading, setUploading] = useState(false)
  const [showLocationSelect, setShowLocationSelect] = useState(false)
  const [selectedLocation, setSelectedLocation] = useState<UploadLocation>(defaultLocation)

  const handleFileSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    // 只允许PDF文件
    if (file.type !== 'application/pdf') {
      alert('目前只支持PDF文件')
      return
    }

    setUploading(true)
    try {
      // 根据选择的位置确定 isPublic
      const isPublic = selectedLocation === 'public' || selectedLocation === 'both'
      await onUpload(file, isPublic)
      setShowLocationSelect(false)
    } finally {
      setUploading(false)
      if (fileInputRef.current) {
        fileInputRef.current.value = ''
      }
    }
  }

  const handleUploadClick = () => {
    if (defaultLocation === 'both') {
      // 如果默认是两者，显示选择对话框
      setShowLocationSelect(true)
    } else {
      // 否则直接触发文件选择
      fileInputRef.current?.click()
    }
  }

  const handleLocationSelect = (location: UploadLocation) => {
    setSelectedLocation(location)
    setShowLocationSelect(false)
    fileInputRef.current?.click()
  }

  return (
    <div className="relative">
      <input
        ref={fileInputRef}
        type="file"
        accept="application/pdf"
        onChange={handleFileSelect}
        className="hidden"
        disabled={uploading}
      />
      
      {showLocationSelect ? (
        <div className="absolute right-0 top-full mt-2 bg-white border border-gray-200 rounded-lg shadow-lg z-10 min-w-[200px]">
          <div className="p-2">
            <div className="flex items-center justify-between mb-2">
              <span className="text-sm font-medium text-gray-700">选择上传位置</span>
              <button
                onClick={() => setShowLocationSelect(false)}
                className="text-gray-400 hover:text-gray-600"
              >
                <X className="h-4 w-4" />
              </button>
            </div>
            <div className="space-y-1">
              <button
                onClick={() => handleLocationSelect('my')}
                className="w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-gray-100 rounded"
              >
                仅我的文献
              </button>
              <button
                onClick={() => handleLocationSelect('public')}
                className="w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-gray-100 rounded"
              >
                仅文献管理
              </button>
              <button
                onClick={() => handleLocationSelect('both')}
                className="w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-gray-100 rounded"
              >
                两者都上传
              </button>
            </div>
          </div>
        </div>
      ) : null}

      <button
        onClick={handleUploadClick}
        disabled={uploading}
        className="flex items-center space-x-2 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:opacity-50 disabled:cursor-not-allowed"
      >
        <Upload className="h-4 w-4" />
        <span>{uploading ? '上传中...' : '上传文件'}</span>
      </button>
    </div>
  )
}
