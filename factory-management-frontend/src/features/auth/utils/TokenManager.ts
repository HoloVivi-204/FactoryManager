import type { AuthUser } from '../../../shared/types'

const ACCESS_TOKEN_KEY = 'factory_access_token'
const AUTH_USER_KEY = 'factory_auth_user'

export const TokenManager = {
  token: () => localStorage.getItem(ACCESS_TOKEN_KEY),

  user: (): AuthUser | null => {
    try {
      return JSON.parse(localStorage.getItem(AUTH_USER_KEY) ?? 'null')
    } catch {
      return null
    }
  },

  save: (user: AuthUser) => {
    localStorage.setItem(ACCESS_TOKEN_KEY, user.token)
    localStorage.setItem(AUTH_USER_KEY, JSON.stringify(user))
  },

  clear: () => {
    localStorage.removeItem(ACCESS_TOKEN_KEY)
    localStorage.removeItem(AUTH_USER_KEY)
  },
}
