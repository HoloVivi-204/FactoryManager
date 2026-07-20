import { get, send } from './client'
import type {
  HrAssignment,
  HrAttendance,
  HrKpi,
  HrLeave,
  HrNotification,
  HrOvertime,
  HrSchedule,
  PageResponse,
} from '../types'

export type HrFilters = {
  fromDate?: string
  toDate?: string
  employeeId?: string | number
  teamId?: string | number
  status?: string
  page?: number
  size?: number
}

const query = (filters: HrFilters = {}) => {
  const params = new URLSearchParams()
  Object.entries(filters).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') params.set(key, String(value))
  })
  const value = params.toString()
  return value ? `?${value}` : ''
}

export const hrApi = {
  schedules: (filters?: HrFilters) =>
    get<PageResponse<HrSchedule>>(`/hr/schedules${query(filters)}`),
  createSchedule: (data: unknown) => send<HrSchedule>('/hr/schedules', 'POST', data),
  updateSchedule: (id: number, data: unknown) =>
    send<HrSchedule>(`/hr/schedules/${id}`, 'PUT', data),
  deleteSchedule: (id: number) => send<void>(`/hr/schedules/${id}`, 'DELETE'),

  attendance: (filters?: HrFilters) =>
    get<PageResponse<HrAttendance>>(`/hr/attendance${query(filters)}`),
  upsertAttendance: (data: unknown) => send<HrAttendance>('/hr/attendance', 'PUT', data),

  leaveRequests: (filters?: HrFilters) =>
    get<PageResponse<HrLeave>>(`/hr/leave-requests${query(filters)}`),
  decideLeave: (id: number, decision: 'APPROVED' | 'REJECTED', comment: string) =>
    send<HrLeave>(`/hr/leave-requests/${id}/decision`, 'PUT', { decision, comment }),

  kpis: (filters?: HrFilters) => get<PageResponse<HrKpi>>(`/hr/kpis${query(filters)}`),
  createKpi: (data: unknown) => send<HrKpi>('/hr/kpis', 'POST', data),
  updateKpi: (id: number, data: unknown) => send<HrKpi>(`/hr/kpis/${id}`, 'PUT', data),

  overtime: (filters?: HrFilters) =>
    get<PageResponse<HrOvertime>>(`/hr/overtime-requests${query(filters)}`),
  createOvertime: (data: unknown) =>
    send<HrOvertime>('/hr/overtime-requests', 'POST', data),
  decideOvertime: (id: number, decision: 'APPROVED' | 'REJECTED', comment: string) =>
    send<HrOvertime>(`/hr/overtime-requests/${id}/decision`, 'PUT', { decision, comment }),

  assignments: (filters?: HrFilters) =>
    get<PageResponse<HrAssignment>>(`/hr/assignments${query(filters)}`),
  createAssignment: (data: unknown) =>
    send<HrAssignment>('/hr/assignments', 'POST', data),
  endAssignment: (id: number, endDate: string) =>
    send<void>(`/hr/assignments/${id}/end?endDate=${encodeURIComponent(endDate)}`, 'PUT'),

  createNotification: (data: unknown) =>
    send<HrNotification>('/hr/notifications', 'POST', data),
}
