import { get, send } from './client'
import type {
  MaintenanceDashboard,
  MaintenanceDowntimeOption,
  MaintenanceMachineOption,
  MaintenancePartUsageItem,
  MaintenancePriority,
  MaintenanceRequestItem,
  MaintenanceRequestStatus,
  MaintenanceScheduleItem,
  MaintenanceStatusHistoryItem,
  MaintenanceWorkOrderItem,
  MaintenanceWorkOrderStatus,
  PageResponse,
} from '../types'

type QueryValue = string | number | boolean | null | undefined

function queryString(values: Record<string, QueryValue>) {
  const params = new URLSearchParams()
  Object.entries(values).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      params.set(key, String(value))
    }
  })
  const query = params.toString()
  return query ? `?${query}` : ''
}

export type MaintenanceRequestFilters = {
  machineId?: number | string
  teamId?: number | string
  status?: MaintenanceRequestStatus | ''
  priority?: MaintenancePriority | ''
  keyword?: string
  fromDate?: string
  toDate?: string
  page?: number
  size?: number
}

export type MaintenanceScheduleFilters = {
  machineId?: number | string
  teamId?: number | string
  active?: boolean | ''
  dueBefore?: string
  page?: number
  size?: number
}

export type MaintenanceWorkOrderFilters = {
  machineId?: number | string
  teamId?: number | string
  status?: MaintenanceWorkOrderStatus | ''
  priority?: MaintenancePriority | ''
  from?: string
  to?: string
  page?: number
  size?: number
}

export type MaintenanceDashboardFilters = {
  teamId?: number | string
  fromDate?: string
  toDate?: string
}

export const maintenanceApi = {
  machineOptions: () =>
    get<MaintenanceMachineOption[]>('/maintenance/options/machines'),

  downtimesByMachine: (machineId: number) =>
    get<MaintenanceDowntimeOption[]>(`/machine-downtime-staging/machine/${machineId}`),

  requests: (filters: MaintenanceRequestFilters = {}) =>
    get<PageResponse<MaintenanceRequestItem>>(`/maintenance/requests${queryString(filters)}`),

  request: (id: number) =>
    get<MaintenanceRequestItem>(`/maintenance/requests/${id}`),

  createRequest: (data: unknown) =>
    send<MaintenanceRequestItem>('/maintenance/requests', 'POST', data),

  updateRequestStatus: (id: number, data: unknown) =>
    send<MaintenanceRequestItem>(`/maintenance/requests/${id}/status`, 'PUT', data),

  schedules: (filters: MaintenanceScheduleFilters = {}) =>
    get<PageResponse<MaintenanceScheduleItem>>(`/maintenance/schedules${queryString(filters)}`),

  schedule: (id: number) =>
    get<MaintenanceScheduleItem>(`/maintenance/schedules/${id}`),

  createSchedule: (data: unknown) =>
    send<MaintenanceScheduleItem>('/maintenance/schedules', 'POST', data),

  updateSchedule: (id: number, data: unknown) =>
    send<MaintenanceScheduleItem>(`/maintenance/schedules/${id}`, 'PUT', data),

  deleteSchedule: (id: number) =>
    send<void>(`/maintenance/schedules/${id}`, 'DELETE'),

  workOrders: (filters: MaintenanceWorkOrderFilters = {}) =>
    get<PageResponse<MaintenanceWorkOrderItem>>(`/maintenance/work-orders${queryString(filters)}`),

  workOrder: (id: number) =>
    get<MaintenanceWorkOrderItem>(`/maintenance/work-orders/${id}`),

  createWorkOrder: (data: unknown) =>
    send<MaintenanceWorkOrderItem>('/maintenance/work-orders', 'POST', data),

  updateWorkOrderStatus: (id: number, data: unknown) =>
    send<MaintenanceWorkOrderItem>(`/maintenance/work-orders/${id}/status`, 'PUT', data),

  addPart: (workOrderId: number, data: unknown) =>
    send<MaintenancePartUsageItem>(`/maintenance/work-orders/${workOrderId}/parts`, 'POST', data),

  deletePart: (workOrderId: number, partId: number) =>
    send<void>(`/maintenance/work-orders/${workOrderId}/parts/${partId}`, 'DELETE'),

  machineHistory: (machineId: number, page = 0, size = 25) =>
    get<PageResponse<MaintenanceStatusHistoryItem>>(
      `/maintenance/machines/${machineId}/status-history${queryString({ page, size })}`,
    ),

  dashboard: (filters: MaintenanceDashboardFilters = {}) =>
    get<MaintenanceDashboard>(`/maintenance/dashboard${queryString(filters)}`),
}
