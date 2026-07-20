import { get, send } from './client'
import type { HrOvertime } from '../types'

export type EmployeePortal = {
  employeeId: number
  employeeCode: string
  employeeName: string
  position: string
  teamName: string
  workingMinutes: number
  overtimeMinutes: number
  unreadNotifications: number
  schedules: any[]
  attendance: any[]
  kpis: any[]
  leaves: any[]
  overtimeRequests: HrOvertime[]
  notifications: any[]
}

export const employeeApi = {
  dashboard: (query = '') => get<EmployeePortal>(`/employee-portal/dashboard${query}`),
  leave: (data: unknown) => send('/employee-portal/leave-requests', 'POST', data),
  overtime: () => get<HrOvertime[]>('/employee-portal/overtime-requests'),
  createOvertime: (data: { workDate: string; requestedMinutes: number; reason: string }) =>
    send<HrOvertime>('/employee-portal/overtime-requests', 'POST', data),
  read: (id: number) => send(`/employee-portal/notifications/${id}/read`, 'PUT'),
}
