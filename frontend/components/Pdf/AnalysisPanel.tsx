'use client'

import { useState, useEffect } from 'react'
import { PdfAnalysis } from '@/lib/api/pdf'
import { Loader2, Save, RefreshCw } from 'lucide-react'
import toast from 'react-hot-toast'

interface AnalysisPanelProps {
  analysis: PdfAnalysis | null
  analyzing: boolean
  onUpdate: (analysis: Partial<PdfAnalysis>) => void
  onRetry: () => void
}

export function AnalysisPanel({
  analysis,
  analyzing,
  onUpdate,
  onRetry,
}: AnalysisPanelProps) {
  const [researchBackground, setResearchBackground] = useState('')
  const [coreContent, setCoreContent] = useState('')
  const [experimentResults, setExperimentResults] = useState('')
  const [additionalInfo, setAdditionalInfo] = useState('')
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (analysis) {
      setResearchBackground(analysis.researchBackground || '')
      setCoreContent(analysis.coreContent || '')
      setExperimentResults(analysis.experimentResults || '')
      setAdditionalInfo(analysis.additionalInfo || '')
    }
  }, [analysis])

  const handleSave = async () => {
    setSaving(true)
    try {
      await onUpdate({
        researchBackground,
        coreContent,
        experimentResults,
        additionalInfo,
      })
    } finally {
      setSaving(false)
    }
  }

  if (analyzing) {
    return (
      <div className="flex flex-col items-center justify-center h-full p-8">
        <Loader2 className="h-12 w-12 animate-spin text-primary-600 mb-4" />
        <p className="text-gray-600">正在分析PDF，请稍候...</p>
      </div>
    )
  }

  if (!analysis) {
    return (
      <div className="flex flex-col items-center justify-center h-full p-8">
        <p className="text-gray-600 mb-4">暂无分析结果</p>
        <button
          onClick={onRetry}
          className="px-4 py-2 bg-primary-600 text-white rounded hover:bg-primary-700"
        >
          开始分析
        </button>
      </div>
    )
  }

  return (
    <div className="p-6 space-y-6 h-full overflow-auto">
      <div className="flex justify-between items-center">
        <h3 className="text-sm font-medium text-gray-500">分析状态: {analysis.analysisStatus}</h3>
        <div className="flex space-x-2">
          <button
            onClick={onRetry}
            className="flex items-center space-x-1 px-3 py-1 text-sm bg-gray-200 rounded hover:bg-gray-300"
          >
            <RefreshCw className="h-4 w-4" />
            <span>重新分析</span>
          </button>
          <button
            onClick={handleSave}
            disabled={saving}
            className="flex items-center space-x-1 px-3 py-1 text-sm bg-primary-600 text-white rounded hover:bg-primary-700 disabled:opacity-50"
          >
            <Save className="h-4 w-4" />
            <span>{saving ? '保存中...' : '保存'}</span>
          </button>
        </div>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          研究背景
        </label>
        <textarea
          value={researchBackground}
          onChange={(e) => setResearchBackground(e.target.value)}
          className="w-full h-32 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
          placeholder="阐述该论文所处领域的研究现状、核心问题及研究动机..."
        />
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          核心内容
        </label>
        <textarea
          value={coreContent}
          onChange={(e) => setCoreContent(e.target.value)}
          className="w-full h-32 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
          placeholder="概括论文提出的方法、模型、算法或关键技术..."
        />
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          实验结果分析
        </label>
        <textarea
          value={experimentResults}
          onChange={(e) => setExperimentResults(e.target.value)}
          className="w-full h-32 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
          placeholder="总结实验设计、关键数据、性能指标及主要结论..."
        />
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          其他补充
        </label>
        <textarea
          value={additionalInfo}
          onChange={(e) => setAdditionalInfo(e.target.value)}
          className="w-full h-32 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
          placeholder="包括创新点、局限性、潜在应用场景及未来研究方向..."
        />
      </div>
    </div>
  )
}

