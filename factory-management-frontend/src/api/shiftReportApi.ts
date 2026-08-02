import { get, send } from './client'
import type { StagingDetailBundle, StagingDetailPath, TableRow } from '../types'

export const detailPaths: StagingDetailPath[] = [
  'machine-downtime-staging',
  'quality-report-staging',
  'material-issue-staging',
  'employee-actual-staging',
]

export const emptyDetailBundle = (): StagingDetailBundle => ({
  'machine-downtime-staging': [],
  'quality-report-staging': [],
  'material-issue-staging': [],
  'employee-actual-staging': [],
})

export const shiftReportApi = {
  createDetail: (path: StagingDetailPath, data: unknown) =>
    send<TableRow>(`/${path}`, 'POST', data),

  updateDetail: (path: StagingDetailPath, id: number, data: unknown) =>
    send<TableRow>(`/${path}/${id}`, 'PUT', data),

  deleteDetail: (path: StagingDetailPath, id: number) => send<void>(`/${path}/${id}`, 'DELETE'),

  details: (path: StagingDetailPath, reportId: number) =>
    get<TableRow[]>(`/${path}/report/${reportId}`),

  bundle: async (reportId: number): Promise<StagingDetailBundle> => {
    const entries = await Promise.all(
      detailPaths.map(
        async (path) => [path, await get<TableRow[]>(`/${path}/report/${reportId}`)] as const,
      ),
    )
    return Object.fromEntries(entries) as StagingDetailBundle
  },
}
