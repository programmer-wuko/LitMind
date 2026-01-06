'use client'

import { useState } from 'react'
import { aiApi } from '@/lib/api/ai'
import { Send, Loader2 } from 'lucide-react'
import toast from 'react-hot-toast'

interface AiQaPanelProps {
  fileId: number
}

export function AiQaPanel({ fileId }: AiQaPanelProps) {
  const [question, setQuestion] = useState('')
  const [answers, setAnswers] = useState<Array<{ question: string; answer: string }>>([])
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!question.trim() || loading) return

    const currentQuestion = question
    setQuestion('')
    setLoading(true)

    try {
      const response = await aiApi.askQuestion({ fileId, question: currentQuestion })
      if (response.code === 200) {
        setAnswers((prev) => [
          { question: currentQuestion, answer: response.data },
          ...prev,
        ])
      } else {
        toast.error('问答失败: ' + (response.message || '未知错误'))
      }
    } catch (error: any) {
      toast.error('问答失败: ' + (error.message || '未知错误'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="h-full flex flex-col">
      <div className="flex-1 overflow-auto p-6 space-y-4">
        {answers.length === 0 ? (
          <div className="text-center text-gray-500 py-12">
            <p>请输入您的问题，AI将基于论文内容为您解答</p>
          </div>
        ) : (
          answers.map((item, index) => (
            <div key={index} className="space-y-2">
              <div className="bg-primary-50 p-3 rounded-lg">
                <p className="text-sm font-medium text-primary-900">问题:</p>
                <p className="text-sm text-primary-800">{item.question}</p>
              </div>
              <div className="bg-gray-50 p-3 rounded-lg">
                <p className="text-sm font-medium text-gray-900">回答:</p>
                <p className="text-sm text-gray-700 whitespace-pre-wrap">{item.answer}</p>
              </div>
            </div>
          ))
        )}
        {loading && (
          <div className="flex items-center justify-center py-4">
            <Loader2 className="h-6 w-6 animate-spin text-primary-600" />
          </div>
        )}
      </div>

      <div className="border-t p-4">
        <form onSubmit={handleSubmit} className="flex space-x-2">
          <input
            type="text"
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
            placeholder="输入您的问题..."
            className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            disabled={loading}
          />
          <button
            type="submit"
            disabled={loading || !question.trim()}
            className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? (
              <Loader2 className="h-5 w-5 animate-spin" />
            ) : (
              <Send className="h-5 w-5" />
            )}
          </button>
        </form>
      </div>
    </div>
  )
}

