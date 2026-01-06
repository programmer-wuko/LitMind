'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { authApi } from '@/lib/api/auth'
import { departmentApi, Department } from '@/lib/api/department'
import { useAuthStore } from '@/lib/store/auth'
import toast from 'react-hot-toast'
import Link from 'next/link'

export default function RegisterPage() {
  const router = useRouter()
  const setAuth = useAuthStore((state) => state.setAuth)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [email, setEmail] = useState('')
  const [nickname, setNickname] = useState('')
  const [departments, setDepartments] = useState<Department[]>([])
  const [selectedDepartmentId, setSelectedDepartmentId] = useState<number | null>(null)
  const [newDepartmentName, setNewDepartmentName] = useState('')
  const [departmentMode, setDepartmentMode] = useState<'select' | 'create'>('select')
  const [loading, setLoading] = useState(false)
  const [loadingDepartments, setLoadingDepartments] = useState(true)

  useEffect(() => {
    loadDepartments()
  }, [])

  const loadDepartments = async () => {
    try {
      setLoadingDepartments(true)
      const response = await departmentApi.getAllDepartments()
      if (response.code === 200) {
        setDepartments(response.data || [])
      } else {
        console.error('加载部门列表失败:', response)
        // 如果加载失败，允许用户手动输入部门名称
        toast('加载部门列表失败，您可以手动创建新部门')
      }
    } catch (error: any) {
      console.error('加载部门列表失败:', error)
      const errorMessage = error.response?.data?.message || error.message || '加载部门列表失败'
      console.error('错误详情:', {
        status: error.response?.status,
        statusText: error.response?.statusText,
        data: error.response?.data,
        url: error.config?.url,
        baseURL: error.config?.baseURL
      })
      // 如果加载失败，允许用户手动输入部门名称
      toast('加载部门列表失败，您可以手动创建新部门')
      // 默认切换到创建模式
      setDepartmentMode('create')
    } finally {
      setLoadingDepartments(false)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)

    // 验证密码
    if (password !== confirmPassword) {
      toast.error('两次输入的密码不一致')
      setLoading(false)
      return
    }

    // 验证部门选择
    if (departmentMode === 'select' && !selectedDepartmentId) {
      toast.error('请选择所属部门')
      setLoading(false)
      return
    }

    if (departmentMode === 'create' && !newDepartmentName.trim()) {
      toast.error('请输入新部门名称')
      setLoading(false)
      return
    }

    try {
      const registerData = {
        username,
        password,
        email: email || undefined,
        nickname: nickname || undefined,
        departmentId: departmentMode === 'select' ? selectedDepartmentId || undefined : undefined,
        newDepartmentName: departmentMode === 'create' ? newDepartmentName.trim() : undefined,
      }

      const response = await authApi.register(registerData)
      if (response.code === 200 && response.data) {
        setAuth(response.data.token, response.data.username, response.data.userId)
        toast.success('注册成功')
        router.push('/dashboard')
      } else {
        toast.error(response.message || '注册失败')
      }
    } catch (error: any) {
      console.error('注册错误详情:', error)
      const errorMessage = error.response?.data?.message || error.message || '注册失败，请检查输入信息'
      toast.error(errorMessage)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100">
      <div className="bg-white p-8 rounded-lg shadow-xl w-full max-w-md">
        <h1 className="text-3xl font-bold text-center mb-8 text-gray-800">
          LitMind
        </h1>
        <p className="text-center text-gray-600 mb-6">科研文献智能分析平台</p>
        
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="username" className="block text-sm font-medium text-gray-700 mb-2">
              用户名 <span className="text-red-500">*</span>
            </label>
            <input
              id="username"
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              minLength={3}
              maxLength={50}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              placeholder="请输入用户名（3-50个字符）"
            />
          </div>

          <div>
            <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-2">
              密码 <span className="text-red-500">*</span>
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              minLength={6}
              maxLength={100}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              placeholder="请输入密码（至少6个字符）"
            />
          </div>

          <div>
            <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700 mb-2">
              确认密码 <span className="text-red-500">*</span>
            </label>
            <input
              id="confirmPassword"
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              placeholder="请再次输入密码"
            />
          </div>

          <div>
            <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-2">
              邮箱
            </label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              placeholder="请输入邮箱（可选）"
            />
          </div>

          <div>
            <label htmlFor="nickname" className="block text-sm font-medium text-gray-700 mb-2">
              昵称
            </label>
            <input
              id="nickname"
              type="text"
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              placeholder="请输入昵称（可选）"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              所属部门 <span className="text-red-500">*</span>
            </label>
            <div className="space-y-2">
              <div className="flex space-x-4">
                <label className="flex items-center">
                  <input
                    type="radio"
                    name="departmentMode"
                    value="select"
                    checked={departmentMode === 'select'}
                    onChange={() => setDepartmentMode('select')}
                    className="mr-2"
                  />
                  选择现有部门
                </label>
                <label className="flex items-center">
                  <input
                    type="radio"
                    name="departmentMode"
                    value="create"
                    checked={departmentMode === 'create'}
                    onChange={() => setDepartmentMode('create')}
                    className="mr-2"
                  />
                  创建新部门
                </label>
              </div>

              {departmentMode === 'select' ? (
                <select
                  value={selectedDepartmentId || ''}
                  onChange={(e) => setSelectedDepartmentId(e.target.value ? Number(e.target.value) : null)}
                  required
                  disabled={loadingDepartments}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent disabled:bg-gray-100"
                >
                  <option value="">请选择部门</option>
                  {departments.map((dept) => (
                    <option key={dept.id} value={dept.id}>
                      {dept.name}
                    </option>
                  ))}
                </select>
              ) : (
                <input
                  type="text"
                  value={newDepartmentName}
                  onChange={(e) => setNewDepartmentName(e.target.value)}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  placeholder="请输入新部门名称"
                />
              )}
            </div>
          </div>

          <button
            type="submit"
            disabled={loading || loadingDepartments}
            className="w-full bg-primary-600 text-white py-2 px-4 rounded-lg hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {loading ? '注册中...' : '注册'}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-gray-500">
          已有账号？{' '}
          <Link href="/login" className="text-primary-600 hover:text-primary-700 font-medium">
            立即登录
          </Link>
        </p>
      </div>
    </div>
  )
}

