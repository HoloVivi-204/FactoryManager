import { get } from '../../../shared/api/client'
import type { ExecutiveDashboard } from '../../../shared/types'

export type ExecutiveDashboardFilters = {
  fromDate: string
  toDate: string
  factoryId?: number
}

export const executiveDashboardApi = {
  get: (filters: ExecutiveDashboardFilters) => {
    const params = new URLSearchParams()
    params.set('fromDate', filters.fromDate)
    params.set('toDate', filters.toDate)
    if (filters.factoryId !== undefined) params.set('factoryId', String(filters.factoryId))
    return get<ExecutiveDashboard>(`/executive-dashboard?${params.toString()}`)
  },
}
