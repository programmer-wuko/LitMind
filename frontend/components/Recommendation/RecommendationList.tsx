'use client'

import { useState } from 'react'
import { RefreshCw, Sparkles } from 'lucide-react'
import { RecommendationCard } from './RecommendationCard'
import { recommendationApi, Recommendation } from '@/lib/api/recommendation'
import toast from 'react-hot-toast'

interface RecommendationListProps {
  recommendations: Recommendation[]
  onRefresh?: () => void
  onImport?: (id: number) => void
  onNotInterested?: (id: number) => void
}

export function RecommendationList({
  recommendations,
  onRefresh,
  onImport,
  onNotInterested,
}: RecommendationListProps) {
  const [refreshing, setRefreshing] = useState(false)

  const handleRefresh = async () => {
    try {
      setRefreshing(true)
      await recommendationApi.generateRecommendations()
      toast.success('推荐已刷新')
      if (onRefresh) {
        onRefresh()
      }
    } catch (error: any) {
      toast.error('刷新推荐失败: ' + (error.message || '未知错误'))
    } finally {
      setRefreshing(false)
    }
  }

  const handleImport = async (id: number) => {
    try {
      if (onImport) {
        await onImport(id)
      }
      toast.success('文献导入成功，正在分析...')
    } catch (error: any) {
      toast.error('导入失败: ' + (error.message || '未知错误'))
    }
  }

  const handleNotInterested = async (id: number) => {
    try {
      await recommendationApi.updateFeedback(id, 'NOT_INTERESTED')
      toast.success('已标记为不感兴趣')
      if (onNotInterested) {
        onNotInterested(id)
      }
    } catch (error: any) {
      toast.error('操作失败: ' + (error.message || '未知错误'))
    }
  }

  if (recommendations.length === 0) {
    return (
      <div className="text-center py-8">
        <Sparkles className="h-12 w-12 mx-auto mb-4 text-gray-400" />
        <p className="text-gray-500 mb-4">暂无推荐文献</p>
        <button
          onClick={handleRefresh}
          disabled={refreshing}
          className="px-4 py-2 bg-primary-600 text-white rounded hover:bg-primary-700 disabled:opacity-50"
        >
          {refreshing ? '生成中...' : '生成推荐'}
        </button>
      </div>
    )
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-2">
          <Sparkles className="h-5 w-5 text-primary-600" />
          <h2 className="text-lg font-semibold">智能推荐</h2>
        </div>
        <button
          onClick={handleRefresh}
          disabled={refreshing}
          className="flex items-center space-x-2 px-3 py-2 text-sm bg-gray-100 text-gray-700 rounded hover:bg-gray-200 disabled:opacity-50"
        >
          <RefreshCw className={`h-4 w-4 ${refreshing ? 'animate-spin' : ''}`} />
          <span>{refreshing ? '刷新中...' : '刷新推荐'}</span>
        </button>
      </div>

      {refreshing && (
        <div className="space-y-3">
          {[1, 2, 3].map((i) => (
            <div key={i} className="bg-gray-100 rounded-lg p-4 animate-pulse">
              <div className="h-4 bg-gray-300 rounded w-3/4 mb-2"></div>
              <div className="h-3 bg-gray-300 rounded w-1/2"></div>
            </div>
          ))}
        </div>
      )}

      {!refreshing && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {recommendations.slice(0, 5).map((rec) => (
            <RecommendationCard
              key={rec.id}
              recommendation={rec}
              onImport={handleImport}
              onNotInterested={handleNotInterested}
            />
          ))}
        </div>
      )}
    </div>
  )
}

