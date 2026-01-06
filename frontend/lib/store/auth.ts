import { create } from 'zustand'
import { persist, createJSONStorage } from 'zustand/middleware'

interface AuthState {
  token: string | null
  username: string | null
  userId: number | null
  setAuth: (token: string, username: string, userId: number) => void
  clearAuth: () => void
  isAuthenticated: () => boolean
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      username: null,
      userId: null,
      setAuth: (token, username, userId) => {
        set({ token, username, userId })
        localStorage.setItem('token', token)
      },
      clearAuth: () => {
        set({ token: null, username: null, userId: null })
        localStorage.removeItem('token')
      },
      isAuthenticated: () => {
        return get().token !== null
      },
    }),
    {
      name: 'auth-storage',
      storage: createJSONStorage(() => localStorage),
    }
  )
)
