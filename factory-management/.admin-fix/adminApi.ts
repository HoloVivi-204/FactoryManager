import { get, send } from './client'

export const adminApi = {
  list: (path: string) => get<any[]>(`/${path}/all`),
  create: (path: string, data: unknown) => send(`/${path}`, 'POST', data),
  update: (path: string, id: number, data: unknown) => send(`/${path}/${id}`, 'PUT', data),
  updateRoles: (id: number, roles: string[]) => send(`/users/${id}/roles`, 'PUT', { roles }),
  register: (data: { employeeId: number; username: string; password: string }) => send<{ userId: number }>(`/auth/register`, 'POST', data),
  resetPassword: (id: number, newPassword: string) => send(`/users/${id}/reset-password`, 'PUT', { newPassword }),
  remove: (path: string, id: number) => send(`/${path}/${id}`, 'DELETE'),
}
