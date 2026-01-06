'use client'

import { useEffect, useState } from 'react'
import { useRouter, usePathname } from 'next/navigation'
import { useAuthStore } from '@/lib/store/auth'
import { userApi } from '@/lib/api/user'
import { LogOut, FileText, Folder, User, BookOpen } from 'lucide-react'
import toast from 'react-hot-toast'

interface NavItem {
  name: string
  href: string
  icon: React.ComponentType<{ className?: string }>
}

const navigation: NavItem[] = [
  { name: '文献管理', href: '/dashboard', icon: BookOpen },
  { name: '我的文献', href: '/dashboard/my-documents', icon: FileText },
  { name: '个人中心', href: '/dashboard/profile', icon: User },
]

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  const router = useRouter()
  const pathname = usePathname()
  const { username, clearAuth } = useAuthStore()
  const [departmentName, setDepartmentName] = useState<string>('加载中...')

  useEffect(() => {
    loadUserInfo()
  }, [])

  const loadUserInfo = async () => {
    try {
      const response = await userApi.getCurrentUser()
      if (response.code === 200 && response.data) {
        setDepartmentName(response.data.departmentName || '未分配部门')
      } else {
        console.error('加载用户信息失败: 响应码', response.code, response)
        setDepartmentName('加载失败')
      }
    } catch (error: any) {
      console.error('加载用户信息失败:', {
        message: error.message,
        response: error.response,
        status: error.response?.status,
        data: error.response?.data,
        url: error.config?.url
      })
      // 不显示错误提示，只设置默认值
      setDepartmentName('加载失败')
    }
  }

  const handleLogout = () => {
    clearAuth()
    toast.success('已退出登录')
    router.push('/login')
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 顶部导航栏 */}
      <nav className="bg-white shadow-sm border-b">
        <div className="flex justify-between items-center h-16 px-6">
          <div className="flex items-center space-x-4">
            <FileText className="h-8 w-8 text-primary-600" />
            <span className="text-xl font-bold text-gray-800">LitMind</span>
            <span className="text-sm text-gray-600 font-medium">{departmentName}</span>
          </div>
          <div className="flex items-center space-x-4">
            <span className="text-sm text-gray-600">欢迎, {username}</span>
            <button
              onClick={handleLogout}
              className="flex items-center space-x-1 px-3 py-1.5 text-sm text-gray-600 hover:text-gray-800 hover:bg-gray-100 rounded"
            >
              <LogOut className="h-4 w-4" />
              <span>退出</span>
            </button>
          </div>
        </div>
      </nav>

      <div className="flex">
        {/* 左侧导航栏 */}
        <aside className="w-64 bg-white shadow-sm border-r min-h-[calc(100vh-4rem)]">
          <div className="p-4">
            <h2 className="text-lg font-semibold mb-4 text-gray-700">导航栏</h2>
            <nav className="space-y-2">
              {navigation.map((item) => {
                const Icon = item.icon
                const isActive = pathname === item.href || 
                  (item.href !== '/dashboard' && pathname?.startsWith(item.href))
                return (
                  <button
                    key={item.name}
                    onClick={() => router.push(item.href)}
                    className={`w-full flex items-center space-x-3 px-4 py-3 rounded-lg text-left transition-colors ${
                      isActive
                        ? 'bg-orange-100 text-orange-700 border border-orange-300'
                        : 'text-gray-700 hover:bg-gray-100'
                    }`}
                  >
                    <Icon className="h-5 w-5" />
                    <span className="font-medium">{item.name}</span>
                  </button>
                )
              })}
            </nav>
          </div>
        </aside>

        {/* 主内容区域 */}
        <main className="flex-1 p-6">
          {children}
        </main>
      </div>
    </div>
  )
}
