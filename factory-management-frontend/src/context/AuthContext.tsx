import { useEffect, useState, type ReactNode } from 'react'
import { authApi } from '../api/authApi'
import { TokenManager } from '../utils/TokenManager'
import { AuthContext } from './auth-context'
import type { AuthUser, Role } from '../types'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(TokenManager.user())
  const [loading, setLoading] = useState(Boolean(TokenManager.token()))

  useEffect(() => {
    const unauthorized = () => setUser(null)
    window.addEventListener('factory:unauthorized', unauthorized)

    if (TokenManager.token()) {
      authApi.me()
        .then((me) => {
          const current = { ...me, token: TokenManager.token()! }
          TokenManager.save(current)
          setUser(current)
        })
        .catch(() => {
          TokenManager.clear()
          setUser(null)
        })
        .finally(() => setLoading(false))
    } else {
      setLoading(false)
    }

    return () => window.removeEventListener('factory:unauthorized', unauthorized)
  }, [])

  async function login(username: string, password: string) {
    const result = await authApi.login(username, password)
    TokenManager.save(result)
    setUser(result)
  }

  async function logout() {
    const token = TokenManager.token()
    try {
      if (token) await authApi.logout(token)
    } finally {
      TokenManager.clear()
      setUser(null)
    }
  }

  const hasRole = (...roles: Role[]) => Boolean(user?.roles.some((role) => roles.includes(role)))

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, hasRole }}>
      {children}
    </AuthContext.Provider>
  )
}
