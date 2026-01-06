'use client'

import { useEffect, useState } from 'react'
import { useParams, useRouter } from 'next/navigation'
import { useAuthStore } from '@/lib/store/auth'
import { pdfApi, PdfAnalysis } from '@/lib/api/pdf'
import { fileApi } from '@/lib/api/file'
import DashboardLayout from '@/components/Layout/DashboardLayout'
import { PdfViewer } from '@/components/Pdf/PdfViewer'
import { AnalysisPanel } from '@/components/Pdf/AnalysisPanel'
import { AiQaPanel } from '@/components/Pdf/AiQaPanel'
import { AttachmentPanel } from '@/components/Pdf/AttachmentPanel'
import { ArrowLeft } from 'lucide-react'
import toast from 'react-hot-toast'

export default function PdfPage() {
  const params = useParams()
  const router = useRouter()
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
  
  // 从URL参数获取fileId
  const fileIdParam = params?.id
  const fileId = fileIdParam ? Number(fileIdParam) : null
  
  // 调试日志
  if (typeof window !== 'undefined' && process.env.NODE_ENV === 'development') {
    console.log('PdfPage - params:', params, 'fileIdParam:', fileIdParam, 'fileId:', fileId)
  }
  
  // 如果fileId无效，返回错误
  if (!fileId || isNaN(fileId)) {
    return (
      <DashboardLayout>
        <div className="text-center py-12 text-red-500">
          无效的文件ID: {String(fileIdParam)}
        </div>
      </DashboardLayout>
    )
  }
  
  const [analysis, setAnalysis] = useState<PdfAnalysis | null>(null)
  const [loading, setLoading] = useState(true)
  const [analyzing, setAnalyzing] = useState(false)
  const [showQaPanel, setShowQaPanel] = useState(false)

  useEffect(() => {
    if (!isAuthenticated()) {
      router.push('/login')
      return
    }
    if (fileId) {
      loadAnalysis()
    }
  }, [fileId, isAuthenticated, router])

  const loadAnalysis = async () => {
    try {
      setLoading(true)
      const response = await pdfApi.getAnalysis(fileId)
      if (response.code === 200) {
        setAnalysis(response.data)
        // 如果没有分析结果，自动触发分析
        if (!response.data || response.data.analysisStatus !== 'COMPLETED') {
          triggerAnalysis()
        }
      }
    } catch (error: any) {
      if (error.response?.status === 404) {
        // 分析记录不存在，触发分析
        triggerAnalysis()
      } else {
        toast.error('加载分析失败: ' + (error.message || '未知错误'))
      }
    } finally {
      setLoading(false)
    }
  }

  const triggerAnalysis = async () => {
    try {
      setAnalyzing(true)
      const response = await pdfApi.analyzePdf(fileId)
      if (response.code === 200) {
        setAnalysis(response.data)
        toast.success('PDF分析完成')
      }
    } catch (error: any) {
      toast.error('PDF分析失败: ' + (error.message || '未知错误'))
    } finally {
      setAnalyzing(false)
    }
  }

  const handleUpdateAnalysis = async (updatedAnalysis: Partial<PdfAnalysis>) => {
    try {
      const response = await pdfApi.updateAnalysis(fileId, updatedAnalysis)
      if (response.code === 200) {
        setAnalysis(response.data)
        toast.success('分析更新成功')
      }
    } catch (error: any) {
      toast.error('更新失败: ' + (error.message || '未知错误'))
    }
  }

  if (loading) {
    return (
      <DashboardLayout>
        <div className="text-center py-12">加载中...</div>
      </DashboardLayout>
    )
  }

  return (
    <DashboardLayout>
      <div className="space-y-4">
        {/* 返回按钮 */}
        <div className="flex items-center">
          <button
            onClick={() => router.push('/dashboard')}
            className="flex items-center space-x-2 px-4 py-2 text-gray-700 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <ArrowLeft className="h-5 w-5" />
            <span>返回文件列表</span>
          </button>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 h-[calc(100vh-14rem)]">
          <div className="bg-white rounded-lg shadow overflow-hidden">
            <div className="p-4 border-b">
              <h2 className="text-lg font-semibold">PDF 阅读器</h2>
            </div>
            <div className="h-[calc(100%-4rem)] overflow-auto">
              <PdfViewer fileId={fileId} />
            </div>
          </div>

          <div className="bg-white rounded-lg shadow overflow-hidden flex flex-col">
            <div className="p-4 border-b flex justify-between items-center">
              <h2 className="text-lg font-semibold">智能分析</h2>
              <button
                onClick={() => setShowQaPanel(!showQaPanel)}
                className="px-4 py-2 bg-primary-600 text-white rounded hover:bg-primary-700"
              >
                {showQaPanel ? '关闭问答' : 'AI问答'}
              </button>
            </div>
            <div className="flex-1 overflow-auto">
              {showQaPanel ? (
                <AiQaPanel fileId={fileId} />
              ) : (
                <AnalysisPanel
                  analysis={analysis}
                  analyzing={analyzing}
                  onUpdate={handleUpdateAnalysis}
                  onRetry={triggerAnalysis}
                />
              )}
            </div>
          </div>
        </div>

        {/* 附件管理面板 */}
        <div className="bg-white rounded-lg shadow">
          <AttachmentPanel fileId={fileId} />
        </div>
      </div>
    </DashboardLayout>
  )
}

