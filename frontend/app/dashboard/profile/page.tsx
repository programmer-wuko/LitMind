'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/lib/store/auth'
import { userApi, UserInfo } from '@/lib/api/user'
import DashboardLayout from '@/components/Layout/DashboardLayout'
import { User, Mail, Calendar, Building2 } from 'lucide-react'
import toast from 'react-hot-toast'

export default function ProfilePage() {
  const router = useRouter()
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
  const { username, userId } = useAuthStore()
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!isAuthenticated()) {
      router.push('/login')
      return
    }
    loadUserInfo()
  }, [isAuthenticated, router])

  const loadUserInfo = async () => {
    try {
      setLoading(true)
      const response = await userApi.getCurrentUser()
      if (response.code === 200 && response.data) {
        setUserInfo(response.data)
      } else {
        console.error('加载用户信息失败: 响应码', response.code, response)
        toast.error('加载用户信息失败: ' + (response.message || '未知错误'))
      }
    } catch (error: any) {
      console.error('加载用户信息失败:', {
        message: error.message,
        response: error.response,
        status: error.response?.status,
        data: error.response?.data,
        url: error.config?.url
      })
      const errorMessage = error.response?.data?.message || error.message || '未知错误'
      toast.error('加载用户信息失败: ' + errorMessage)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <DashboardLayout>
        <div className="max-w-4xl mx-auto">
          <div className="bg-white rounded-lg shadow p-6">
            <div className="text-center py-12 text-gray-500">加载中...</div>
          </div>
        </div>
      </DashboardLayout>
    )
  }

  return (
    <DashboardLayout>
      <div className="max-w-4xl mx-auto">
        <div className="bg-white rounded-lg shadow p-6">
          <h1 className="text-2xl font-bold mb-6">个人中心</h1>
          
          <div className="space-y-6">
            {/* 基本信息 */}
            <div className="border-b pb-6">
              <h2 className="text-lg font-semibold mb-4">基本信息</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="flex items-center space-x-3">
                  <User className="h-5 w-5 text-gray-400" />
                  <div>
                    <label className="text-sm text-gray-500">用户名</label>
                    <p className="text-gray-900 font-medium">{userInfo?.username || username || '未设置'}</p>
                  </div>
                </div>
                <div className="flex items-center space-x-3">
                  <Mail className="h-5 w-5 text-gray-400" />
                  <div>
                    <label className="text-sm text-gray-500">用户ID</label>
                    <p className="text-gray-900 font-medium">{userInfo?.id || userId || '未设置'}</p>
                  </div>
                </div>
                <div className="flex items-center space-x-3">
                  <Building2 className="h-5 w-5 text-gray-400" />
                  <div>
                    <label className="text-sm text-gray-500">所属部门</label>
                    <p className="text-gray-900 font-medium">{userInfo?.departmentName || '未分配部门'}</p>
                  </div>
                </div>
                {userInfo?.email && (
                  <div className="flex items-center space-x-3">
                    <Mail className="h-5 w-5 text-gray-400" />
                    <div>
                      <label className="text-sm text-gray-500">邮箱</label>
                      <p className="text-gray-900 font-medium">{userInfo.email}</p>
                    </div>
                  </div>
                )}
                {userInfo?.nickname && (
                  <div className="flex items-center space-x-3">
                    <User className="h-5 w-5 text-gray-400" />
                    <div>
                      <label className="text-sm text-gray-500">昵称</label>
                      <p className="text-gray-900 font-medium">{userInfo.nickname}</p>
                    </div>
                  </div>
                )}
              </div>
            </div>

            {/* 各类基本信息 */}
            <div>
              <h2 className="text-lg font-semibold mb-4">各类基本信息</h2>
              <div className="bg-blue-50 rounded-lg p-6 text-center">
                <p className="text-blue-700 font-medium">更多功能开发中...</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </DashboardLayout>
  )
}

