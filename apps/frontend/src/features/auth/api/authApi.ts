import { get, publicPost } from '../../../shared/api/client'
import type { AuthUser } from '../../../shared/types'

export const authApi = {
  login: (username: string, password: string) =>
    publicPost<AuthUser>('/auth/login', { username, password }),

  me: () => get<AuthUser>('/auth/me'),

  logout: (token: string) => publicPost<void>('/auth/logout', { token }),
}
