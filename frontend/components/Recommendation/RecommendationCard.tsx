'use client'

import { FileText, ExternalLink, Download, X, CheckCircle } from 'lucide-react'
import { Recommendation } from '@/lib/api/recommendation'
import { useRouter } from 'next/navigation'

interface RecommendationCardProps {
  recommendation: Recommendation
  onImport?: (id: number) => void
  onNotInterested?: (id: number) => void
  onView?: (id: number) => void
}

export function RecommendationCard({
  recommendation,
  onImport,
  onNotInterested,
  onView,
}: RecommendationCardProps) {
  const router = useRouter()

  const handleView = () => {
    if (recommendation.recommendedFileId) {
      // 内部文件，跳转到PDF分析页面
      router.push(`/pdf/${recommendation.recommendedFileId}`)
      onView?.(recommendation.id)
    } else if (recommendation.paperUrl) {
      // 外部文献，验证URL有效性后再打开链接
      const url = recommendation.paperUrl
      // 检查是否是无效的示例URL（包含"example"）
      if (url.includes('/example') || url.includes('example')) {
        console.warn('无效的推荐链接:', url)
        alert('抱歉，该推荐链接无效。请刷新推荐列表获取新的推荐。')
        return
      }
      // 验证URL格式
      try {
        new URL(url)
        window.open(url, '_blank')
      } catch (e) {
        console.error('无效的URL:', url)
        alert('抱歉，该推荐链接格式无效。')
      }
    }
  }

  const handleImport = () => {
    if (onImport) {
      onImport(recommendation.id)
    }
  }

  const handleNotInterested = () => {
    if (onNotInterested) {
      onNotInterested(recommendation.id)
    }
  }

  const isInternal = !!recommendation.recommendedFileId
  const isExternal = !!recommendation.externalPaperId

  return (
    <div className="bg-white border rounded-lg p-4 hover:shadow-md transition-shadow">
      <div className="flex items-start justify-between mb-2">
        <div className="flex-1">
          <h3 className="font-semibold text-gray-900 mb-1 line-clamp-2">
            {recommendation.paperTitle || '未命名文献'}
          </h3>
          {recommendation.paperAuthors && (
            <p className="text-sm text-gray-600 mb-2 line-clamp-1">
              {recommendation.paperAuthors}
            </p>
          )}
          {recommendation.paperSource && (
            <span className="inline-block px-2 py-1 text-xs bg-gray-100 text-gray-600 rounded">
              {recommendation.paperSource}
            </span>
          )}
        </div>
        {isInternal && (
          <CheckCircle className="h-5 w-5 text-green-500 flex-shrink-0 ml-2" />
        )}
      </div>

      {recommendation.recommendationReason && (
        <p className="text-sm text-gray-700 mb-3 line-clamp-2">
          {recommendation.recommendationReason}
        </p>
      )}

      <div className="flex items-center space-x-2 mt-3">
        {isInternal ? (
          <button
            onClick={handleView}
            className="flex items-center space-x-1 px-3 py-1.5 text-sm bg-primary-600 text-white rounded hover:bg-primary-700"
          >
            <FileText className="h-4 w-4" />
            <span>查看</span>
          </button>
        ) : isExternal ? (
          <>
            <button
              onClick={handleImport}
              className="flex items-center space-x-1 px-3 py-1.5 text-sm bg-primary-600 text-white rounded hover:bg-primary-700"
            >
              <Download className="h-4 w-4" />
              <span>导入分析</span>
            </button>
            <button
              onClick={handleView}
              className="flex items-center space-x-1 px-3 py-1.5 text-sm bg-gray-200 text-gray-700 rounded hover:bg-gray-300"
            >
              <ExternalLink className="h-4 w-4" />
              <span>查看原文</span>
            </button>
          </>
        ) : null}

        <button
          onClick={handleNotInterested}
          className="flex items-center space-x-1 px-3 py-1.5 text-sm text-gray-600 hover:text-gray-800 hover:bg-gray-100 rounded"
          title="不感兴趣"
        >
          <X className="h-4 w-4" />
        </button>
      </div>
    </div>
  )
}

