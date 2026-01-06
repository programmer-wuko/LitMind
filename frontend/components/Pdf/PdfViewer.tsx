'use client'

import { useState } from 'react'
import { Document, Page, pdfjs } from 'react-pdf'
import 'react-pdf/dist/esm/Page/AnnotationLayer.css'
import 'react-pdf/dist/esm/Page/TextLayer.css'
import { ChevronLeft, ChevronRight, ZoomIn, ZoomOut } from 'lucide-react'

// 配置PDF.js worker (react-pdf v7)
pdfjs.GlobalWorkerOptions.workerSrc = `//cdnjs.cloudflare.com/ajax/libs/pdf.js/${pdfjs.version}/pdf.worker.min.js`

interface PdfViewerProps {
  fileId: number
}

export function PdfViewer({ fileId }: PdfViewerProps) {
  const [numPages, setNumPages] = useState<number>(0)
  const [pageNumber, setPageNumber] = useState(1)
  const [scale, setScale] = useState(1.0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // 验证fileId
  if (!fileId || isNaN(Number(fileId))) {
    return (
      <div className="flex items-center justify-center h-full text-red-500">
        无效的文件ID: {String(fileId)}
      </div>
    )
  }

  // 构建PDF下载URL（使用与apiClient相同的逻辑）
  const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api'
  const pdfUrl = `${apiUrl}/files/${fileId}/download`
  
  // 调试日志（开发环境）
  if (typeof window !== 'undefined' && process.env.NODE_ENV === 'development') {
    console.log('PDF Viewer - fileId:', fileId, 'pdfUrl:', pdfUrl)
  }

  const onDocumentLoadSuccess = ({ numPages }: { numPages: number }) => {
    setNumPages(numPages)
    setLoading(false)
    setError(null)
  }

  const onDocumentLoadError = (error: Error) => {
    const errorMsg = `PDF加载失败: ${error.message}\nURL: ${pdfUrl}\nFileId: ${fileId}`
    console.error('PDF加载错误:', errorMsg, error)
    setError(errorMsg)
    setLoading(false)
  }

  const goToPrevPage = () => {
    setPageNumber((prev) => Math.max(1, prev - 1))
  }

  const goToNextPage = () => {
    setPageNumber((prev) => Math.min(numPages, prev + 1))
  }

  const zoomIn = () => {
    setScale((prev) => Math.min(3.0, prev + 0.2))
  }

  const zoomOut = () => {
    setScale((prev) => Math.max(0.5, prev - 0.2))
  }

  if (error) {
    return (
      <div className="flex items-center justify-center h-full text-red-500">
        {error}
      </div>
    )
  }

  return (
    <div className="h-full flex flex-col">
      <div className="flex items-center justify-between p-4 border-b bg-gray-50">
        <div className="flex items-center space-x-2">
          <button
            onClick={goToPrevPage}
            disabled={pageNumber <= 1}
            className="p-2 rounded hover:bg-gray-200 disabled:opacity-50"
          >
            <ChevronLeft className="h-5 w-5" />
          </button>
          <span className="text-sm">
            第 {pageNumber} 页 / 共 {numPages} 页
          </span>
          <button
            onClick={goToNextPage}
            disabled={pageNumber >= numPages}
            className="p-2 rounded hover:bg-gray-200 disabled:opacity-50"
          >
            <ChevronRight className="h-5 w-5" />
          </button>
        </div>
        <div className="flex items-center space-x-2">
          <button
            onClick={zoomOut}
            className="p-2 rounded hover:bg-gray-200"
          >
            <ZoomOut className="h-5 w-5" />
          </button>
          <span className="text-sm w-16 text-center">{Math.round(scale * 100)}%</span>
          <button
            onClick={zoomIn}
            className="p-2 rounded hover:bg-gray-200"
          >
            <ZoomIn className="h-5 w-5" />
          </button>
        </div>
      </div>

      <div className="flex-1 overflow-auto bg-gray-100 p-4">
        {loading && (
          <div className="flex items-center justify-center h-full">
            <div className="text-gray-500">加载PDF中...</div>
          </div>
        )}
        <div className="flex justify-center">
          <Document
            file={pdfUrl}
            onLoadSuccess={onDocumentLoadSuccess}
            onLoadError={onDocumentLoadError}
            loading={<div className="text-gray-500">加载中...</div>}
            options={{
              httpHeaders: {
                Authorization: `Bearer ${localStorage.getItem('token')}`,
              },
            }}
          >
            <Page
              pageNumber={pageNumber}
              scale={scale}
              renderTextLayer={true}
              renderAnnotationLayer={true}
            />
          </Document>
        </div>
      </div>
    </div>
  )
}

