import { get, send } from './client'
import type { HrAttendance, HrKpi, HrLeave, HrNotification, HrOvertime, HrSchedule } from '../types'

export type EmployeePortal = {
  employeeId: number
  employeeCode: string
  employeeName: string
  position: string
  teamName: string
  workingMinutes: number
  overtimeMinutes: number
  unreadNotifications: number
  schedules: HrSchedule[]
  attendance: HrAttendance[]
  kpis: HrKpi[]
  leaves: HrLeave[]
  overtimeRequests: HrOvertime[]
  notifications: HrNotification[]
}

export const employeeApi = {
  dashboard: (query = '') => get<EmployeePortal>(`/employee-portal/dashboard${query}`),
  leave: (data: unknown) => send('/employee-portal/leave-requests', 'POST', data),
  overtime: () => get<HrOvertime[]>('/employee-portal/overtime-requests'),
  createOvertime: (data: { workDate: string; requestedMinutes: number; reason: string }) =>
    send<HrOvertime>('/employee-portal/overtime-requests', 'POST', data),
  read: (id: number) => send(`/employee-portal/notifications/${id}/read`, 'PUT'),
}
