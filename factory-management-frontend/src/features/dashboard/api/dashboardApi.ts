import { get, send } from '../../../shared/api/client'
import { shiftReportApi } from '../../production-reports/api/shiftReportApi'
import type {
  DashboardSummary,
  Machine,
  ProductionReport,
  ProductionReportDetails,
  StagingDetailPath,
  StagingReport,
  TableRow,
} from '../../../shared/types'

const suffix = (query = '') => {
  if (!query) return ''
  return query.startsWith('?') ? query : `?${query}`
}

export const dashboardApi = {
  summary: () => get<DashboardSummary>('/production-reports/dashboard/my-scope'),

  reports: (query = '') =>
    get<ProductionReport[]>(`/production-reports/search/my-scope${suffix(query)}`),

  productionReportDetails: (id: number) =>
    get<ProductionReportDetails>(`/production-reports/${id}/details`),

  stagingMyScope: () => get<StagingReport[]>('/production-report-staging/my-scope'),

  getStaging: (id: number) => get<StagingReport>(`/production-report-staging/${id}`),

  submitted: async () => {
    const reports = await get<StagingReport[]>('/production-report-staging/my-scope')
    return reports.filter((report) => report.status === 'SUBMITTED')
  },

  stagingDetailsInScope: async (path: StagingDetailPath): Promise<TableRow[]> => {
    const reports = await get<StagingReport[]>('/production-report-staging/my-scope')
    const groups = await Promise.all(
      reports.map(async (report) => {
        const details = await shiftReportApi.details(path, report.id)
        return details.map((detail) => ({
          ...detail,
          reportId: report.id,
          reportDate: report.reportDate,
          reportStatus: report.status,
          teamName: detail.teamName ?? report.teamName,
          machineCode: detail.machineCode ?? report.machineCode,
        }))
      }),
    )
    return groups.flat()
  },

  machinesInScope: async (): Promise<Machine[]> => {
    const reports = await get<StagingReport[]>('/production-report-staging/my-scope')
    const unique = new Map<number, Machine>()
    reports.forEach((report) => {
      if (!unique.has(report.machineId)) {
        unique.set(report.machineId, {
          id: report.machineId,
          code: report.machineCode,
          name: report.machineName ?? report.machineCode,
          teamName: report.teamName,
        })
      }
    })
    return [...unique.values()]
  },

  master: (path: string) => get<TableRow[]>(`/${path}/all`),
  departmentsByFactory: (factoryId: number) => get<TableRow[]>(`/departments/factory/${factoryId}`),
  linesByDepartment: (departmentId: number) =>
    get<TableRow[]>(`/production-lines/department/${departmentId}`),
  teamsByLine: (lineId: number) => get<TableRow[]>(`/teams/production-line/${lineId}`),
  employeesByTeam: (teamId: number) => get<TableRow[]>(`/employees/team/${teamId}`),
  machinesByTeam: (teamId: number) => get<TableRow[]>(`/machines/team/${teamId}`),

  createStaging: (data: unknown) => send<StagingReport>('/production-report-staging', 'POST', data),
  updateStaging: (id: number, data: unknown) =>
    send<StagingReport>(`/production-report-staging/${id}`, 'PUT', data),
  deleteStaging: (id: number) => send<void>(`/production-report-staging/${id}`, 'DELETE'),
  returnToDraft: (id: number) =>
    send<StagingReport>(`/production-report-staging/${id}/return-to-draft`, 'PUT'),
  submit: (id: number) => send<StagingReport>(`/production-report-staging/${id}/submit`, 'PUT'),
  requestChange: (id: number, comment: string) =>
    send<StagingReport>(`/production-report-staging/${id}/request-change`, 'PUT', { comment }),
  approve: (id: number, remark: string) =>
    send<ProductionReport>(`/production-report-staging/${id}/approve`, 'POST', { remark }),
}
