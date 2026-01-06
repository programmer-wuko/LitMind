'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/lib/store/auth'

export default function HomePage() {
  const router = useRouter()
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)

  useEffect(() => {
    // 检查是否已登录
    if (isAuthenticated()) {
      router.push('/dashboard')
    } else {
      router.push('/login')
    }
  }, [router, isAuthenticated])

  // 显示加载状态
  return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="text-gray-500">加载中...</div>
    </div>
  )
}

