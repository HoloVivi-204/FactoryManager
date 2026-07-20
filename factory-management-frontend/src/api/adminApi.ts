import { get, send } from './client'

export type DepartmentTypeOption = {
  type: string
  codeSuffix: string
  name: string
  description: string
}

export type DataScopeType = 'FACTORY' | 'DEPARTMENT' | 'PRODUCTION_LINE' | 'TEAM'

export type UserDataScope = {
  id: number
  userId: number
  scopeType: DataScopeType
  scopeId: number
  scopeCode: string
  scopeName: string
}

export const adminApi = {
  list: (path: string) => get<any[]>(`/${path}/all`),
  departmentTypes: () => get<DepartmentTypeOption[]>('/departments/types'),
  employeesByTeam: (teamId: number) => get<any[]>(`/employees/team/${teamId}`),
  create: (path: string, data: unknown) => send(`/${path}`, 'POST', data),
  update: (path: string, id: number, data: unknown) => send(`/${path}/${id}`, 'PUT', data),
  updateRoles: (id: number, roles: string[]) => send(`/users/${id}/roles`, 'PUT', { roles }),
  register: (data: { employeeId: number; username: string; password: string }) => send<{ userId: number }>(`/auth/register`, 'POST', data),
  resetPassword: (id: number, newPassword: string) => send(`/users/${id}/reset-password`, 'PUT', { newPassword }),
  dataScopes: (userId: number) => get<UserDataScope[]>(`/users/${userId}/data-scopes`),
  addDataScope: (userId: number, scopeType: DataScopeType, scopeId: number) =>
    send<UserDataScope>(`/users/${userId}/data-scopes`, 'POST', { scopeType, scopeId }),
  removeDataScope: (userId: number, dataScopeId: number) =>
    send(`/users/${userId}/data-scopes/${dataScopeId}`, 'DELETE'),
  assignTeamLeader: (teamId: number, employeeId: number) =>
    send(`/teams/${teamId}/leader`, 'PUT', { employeeId }),
  removeTeamLeader: (teamId: number) => send(`/teams/${teamId}/leader`, 'DELETE'),
  remove: (path: string, id: number) => send(`/${path}/${id}`, 'DELETE'),
}
