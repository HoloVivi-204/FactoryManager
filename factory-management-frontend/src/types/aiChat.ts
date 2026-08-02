import type { Role } from './index'

export type AiChatDataStatus =
  | 'OFFICIAL'
  | 'TEMPORARY_UNCONFIRMED'
  | 'OFFICIAL_WITH_TEMPORARY'
  | 'NO_BUSINESS_DATA'
  | string

export type AiChatSource = {
  type: string
  label: string
  dataStatus: AiChatDataStatus
  recordCount: number
  asOf?: string | null
}

export type AiDashboardWidget = {
  viewType: 'PROGRESS' | 'RATIO' | 'GROUPED_BAR' | 'BAR' | 'LINE' | 'DONUT' | 'TABLE' | string
  title: string
  context?: string | null
  categoryKey?: string | null
  series?: Array<{
    key: string
    label: string
    unit?: string | null
    tone?: string | null
  }>
  columns?: Array<{
    key: string
    label: string
    format?:
      | 'text'
      | 'number'
      | 'percent'
      | 'currency'
      | 'duration'
      | 'date'
      | 'datetime'
      | 'status'
      | string
  }>
  rows?: Array<Record<string, unknown>>
  numerator?: number | null
  denominator?: number | null
  unit?: string | null
  numeratorLabel?: string | null
  denominatorLabel?: string | null
  tone?: string | null
}

export type AiRecommendation = {
  priority: 'HIGH' | 'MEDIUM' | 'LOW' | string
  title: string
  action: string
  evidence: string
  expectedImpact: string
  estimated: boolean
}

export type AiDashboard = {
  title: string
  subtitle: string
  kpis: Array<{
    label: string
    value: unknown
    unit?: string
    tone?: string
  }>
  bars: Array<{
    label: string
    value: number
    unit?: string
  }>
  columns: Array<{
    key: string
    label: string
    format?: 'text' | 'number' | 'date' | 'status' | string
  }>
  rows: Array<Record<string, unknown>>
  ratios?: Array<{
    key: string
    label: string
    numerator: number
    denominator: number
    unit?: string
    numeratorLabel?: string
    denominatorLabel?: string
    context?: string
    tone?: string
  }>
  widgets?: AiDashboardWidget[]
}

export type AiChatResponse = {
  answer: string
  dataStatus: AiChatDataStatus
  model?: string | null
  workspaceRole?: Role | null
  toolsUsed: string[]
  sources: AiChatSource[]
  warnings: string[]
  dashboards?: AiDashboard[]
  recommendations?: AiRecommendation[]
  answeredAt: string
}

export type AiChatRequest = {
  message: string
  workspaceRole: Role
}
