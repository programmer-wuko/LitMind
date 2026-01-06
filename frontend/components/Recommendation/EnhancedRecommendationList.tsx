'use client'

import { useState } from 'react'
import { RefreshCw, Sparkles, TrendingUp, BookOpen, Star } from 'lucide-react'
import { RecommendationCard } from './RecommendationCard'
import { recommendationApi, Recommendation } from '@/lib/api/recommendation'
import toast from 'react-hot-toast'

interface EnhancedRecommendationListProps {
  recommendations: Recommendation[]
  onRefresh?: () => void
  onImport?: (id: number) => void
  onNotInterested?: (id: number) => void
}

export function EnhancedRecommendationList({
  recommendations,
  onRefresh,
  onImport,
  onNotInterested,
}: EnhancedRecommendationListProps) {
  const [refreshing, setRefreshing] = useState(false)
  const [filter, setFilter] = useState<'all' | 'hot' | 'new'>('all')

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

  // 过滤推荐
  const filteredRecommendations = recommendations.filter((rec) => {
    if (filter === 'hot') {
      // 热门推荐：可以根据评分、点击量等筛选
      return rec.score && rec.score > 0.7
    }
    if (filter === 'new') {
      // 最新推荐：可以根据创建时间筛选
      const daysSinceCreation = (Date.now() - new Date(rec.createdAt).getTime()) / (1000 * 60 * 60 * 24)
      return daysSinceCreation < 7
    }
    return true
  })

  if (recommendations.length === 0) {
    return (
      <div className="text-center py-12">
        <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-primary-100 mb-4">
          <Sparkles className="h-8 w-8 text-primary-600" />
        </div>
        <p className="text-gray-500 mb-4 text-lg">暂无推荐文献</p>
        <p className="text-gray-400 mb-6 text-sm">点击下方按钮生成个性化推荐</p>
        <button
          onClick={handleRefresh}
          disabled={refreshing}
          className="px-6 py-3 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:opacity-50 transition-colors flex items-center space-x-2 mx-auto"
        >
          {refreshing ? (
            <>
              <RefreshCw className="h-5 w-5 animate-spin" />
              <span>生成中...</span>
            </>
          ) : (
            <>
              <Sparkles className="h-5 w-5" />
              <span>生成推荐</span>
            </>
          )}
        </button>
      </div>
    )
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-3">
          <div className="inline-flex items-center justify-center w-10 h-10 rounded-lg bg-primary-100">
            <Sparkles className="h-6 w-6 text-primary-600" />
          </div>
          <div>
            <h2 className="text-xl font-bold text-gray-900">智能推荐</h2>
            <p className="text-sm text-gray-500">基于您的阅读习惯和兴趣推荐</p>
          </div>
        </div>
        <button
          onClick={handleRefresh}
          disabled={refreshing}
          className="flex items-center space-x-2 px-4 py-2 text-sm bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 disabled:opacity-50 transition-colors"
        >
          <RefreshCw className={`h-4 w-4 ${refreshing ? 'animate-spin' : ''}`} />
          <span>{refreshing ? '刷新中...' : '刷新推荐'}</span>
        </button>
      </div>

      {/* 筛选标签 */}
      <div className="flex items-center space-x-2">
        <button
          onClick={() => setFilter('all')}
          className={`px-4 py-2 text-sm rounded-lg transition-colors ${
            filter === 'all'
              ? 'bg-primary-600 text-white'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          }`}
        >
          <BookOpen className="h-4 w-4 inline mr-1" />
          全部
        </button>
        <button
          onClick={() => setFilter('hot')}
          className={`px-4 py-2 text-sm rounded-lg transition-colors ${
            filter === 'hot'
              ? 'bg-primary-600 text-white'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          }`}
        >
          <TrendingUp className="h-4 w-4 inline mr-1" />
          热门
        </button>
        <button
          onClick={() => setFilter('new')}
          className={`px-4 py-2 text-sm rounded-lg transition-colors ${
            filter === 'new'
              ? 'bg-primary-600 text-white'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          }`}
        >
          <Star className="h-4 w-4 inline mr-1" />
          最新
        </button>
        <span className="text-sm text-gray-500 ml-2">
          共 {filteredRecommendations.length} 条推荐
        </span>
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
        <>
          {filteredRecommendations.length === 0 ? (
            <div className="text-center py-8 text-gray-500">
              <p>当前筛选条件下暂无推荐</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {filteredRecommendations.slice(0, 6).map((rec) => (
                <RecommendationCard
                  key={rec.id}
                  recommendation={rec}
                  onImport={handleImport}
                  onNotInterested={handleNotInterested}
                />
              ))}
            </div>
          )}
        </>
      )}
    </div>
  )
}

