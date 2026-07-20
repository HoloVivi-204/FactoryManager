export type Role =
  | 'ADMIN'
  | 'DIRECTOR'
  | 'FACTORY_MANAGER'
  | 'DEPARTMENT_MANAGER'
  | 'FINANCE'
  | 'PRODUCTION_MANAGER'
  | 'TEAM_LEADER'
  | 'EMPLOYEE'

export type PageKey =
  | 'admin-master'
  | 'admin-factories'
  | 'admin-departments'
  | 'admin-lines'
  | 'admin-teams'
  | 'admin-employees'
  | 'admin-shifts'
  | 'admin-machine-types'
  | 'admin-machines'
  | 'admin-downtime-reasons'
  | 'admin-quality-types'
  | 'admin-materials'
  | 'admin-users'
  | 'overview'
  | 'schedule'
  | 'attendance'
  | 'kpi'
  | 'leave'
  | 'overtime'
  | 'assignments'
  | 'notifications'
  | 'entry'
  | 'reports'
  | 'approval'
  | 'machines'
  | 'downtime'
  | 'quality'
  | 'people'
  | 'materials'
  | 'finance'
  | 'warehouses'
  | 'inventory'
  | 'maintenance-dashboard'
  | 'maintenance-requests'
  | 'maintenance-schedules'
  | 'maintenance-work-orders'
  | 'maintenance-history'

export type ExecutiveFinancialOverview = {
  revenue: number
  expense: number
  profit: number
  profitMarginPercent: number
  overdueReceivable: number
  overdueReceivableCount: number
  overduePayable: number
  overduePayableCount: number
  postedRecordCount: number
}

export type ExecutiveProductionOverview = {
  officialReportCount: number
  plannedQuantity: number
  actualQuantity: number
  goodQuantity: number
  defectQuantity: number
  workingMinutes: number
  downtimeMinutes: number
  planAttainmentPercent: number
  productivityPerHour: number
  defectRatePercent: number
  qualityPercent: number
  availabilityPercent: number
  performancePercent: number
  oeePercent: number
}

export type ExecutiveTrendPoint = {
  period: string
  periodStart: string
  periodEnd: string
  revenue: number
  expense: number
  profit: number
  plannedQuantity: number
  actualQuantity: number
  goodQuantity: number
  defectQuantity: number
  planAttainmentPercent: number
  productivityPerHour: number
  qualityPercent: number
  oeePercent: number
}

export type ExecutiveFactoryPerformance = {
  factoryId: number
  factoryName: string
  revenue: number
  expense: number
  profit: number
  plannedQuantity: number
  actualQuantity: number
  goodQuantity: number
  defectQuantity: number
  planAttainmentPercent: number
  productivityPerHour: number
  qualityPercent: number
  oeePercent: number
}

export type ExecutiveRisk = {
  code: string
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL' | string
  domain: string
  title: string
  description: string
  metricValue: number
  unit: string
  threshold: number
  affectedCount: number
}

export type ExecutiveDecision = {
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL' | string
  domain: string
  title: string
  rationale: string
  recommendedAction: string
  relatedRiskCode: string
}

export type ExecutiveDashboard = {
  fromDate: string
  toDate: string
  asOfDate: string
  scopeType: 'COMPANY' | 'FACTORY' | string
  factoryId?: number
  scopeName: string
  trendGranularity: 'DAY' | 'MONTH' | string
  kpis: {
    financial: ExecutiveFinancialOverview
    production: ExecutiveProductionOverview
  }
  trends: ExecutiveTrendPoint[]
  factories: ExecutiveFactoryPerformance[]
  risks: ExecutiveRisk[]
  decisions: ExecutiveDecision[]
  generatedAt: string
}

export type ProductionReportStatus =
  | 'DRAFT'
  | 'SUBMITTED'
  | 'CHANGE_REQUESTED'
  | 'APPROVED'
  | 'LOCKED'

export type AuthUser = {
  token: string
  authenticated: boolean
  expiresIn: number
  userId: number
  employeeId: number | null
  employeeCode: string
  employeeName: string
  username: string
  roles: Role[]
}

export type DashboardSummary = {
  reportCount: number
  plannedQuantity: number
  actualQuantity: number
  goodQuantity: number
  defectQuantity: number
  downtimeMinutes: number
  averageAvailability: number
  averagePerformance: number
  averageQuality: number
  averageOee: number
  scopeType: string
  scopeId?: number
  scopeName: string
}

export type ProductionReport = {
  id: number
  reportNo: string
  reportDate: string
  shiftId?: number
  shiftName: string
  factoryId?: number
  factoryName: string
  departmentId?: number
  departmentName?: string
  productionLineId?: number
  productionLineName?: string
  teamId?: number
  teamName: string
  machineId?: number
  machineCode: string
  machineName: string
  plannedQuantity: number
  actualQuantity: number
  goodQuantity: number
  defectQuantity: number
  downtimeMinutes: number
  availability: number
  performance: number
  quality: number
  oee: number
}

export type StagingReport = {
  id: number
  reportDate: string
  shiftId: number
  shiftCode?: string
  shiftName: string
  factoryId: number
  factoryCode?: string
  factoryName: string
  departmentId: number
  departmentCode?: string
  departmentName?: string
  productionLineId: number
  productionLineCode?: string
  productionLineName?: string
  teamId: number
  teamCode?: string
  teamName: string
  leaderEmployeeId: number
  leaderEmployeeCode?: string
  leaderEmployeeName?: string
  machineId: number
  machineCode: string
  machineName?: string
  plannedQuantity: number
  actualQuantity: number
  goodQuantity: number
  defectQuantity: number
  workingMinutes: number
  downtimeMinutes: number
  note?: string
  status: ProductionReportStatus
  reviewComment?: string
  submittedAt?: string
  reviewedAt?: string
}

export type Machine = {
  id: number
  code: string
  name: string
  serialNumber?: string
  status?: string
  machineTypeName?: string
  teamName?: string
  installationDate?: string
  active?: boolean
}

export type TableRow = Record<string, any>

export type StagingDetailPath =
  | 'machine-downtime-staging'
  | 'quality-report-staging'
  | 'material-issue-staging'
  | 'employee-actual-staging'

export type StagingDetailBundle = Record<StagingDetailPath, TableRow[]>

export type ProductionReportDetails = {
  report: ProductionReport
  downtimes: TableRow[]
  qualityErrors: TableRow[]
  materialIssues: TableRow[]
  employees: TableRow[]
}

export type PageResponse<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

export type HrSchedule = {
  id: number
  employeeId: number
  employeeCode: string
  employeeName: string
  teamId: number
  teamName: string
  shiftId: number
  shiftCode: string
  shiftName: string
  workDate: string
  note?: string
  active: boolean
  version: number
}

export type HrAttendance = {
  id: number
  employeeId: number
  employeeCode: string
  employeeName: string
  teamId: number
  teamName: string
  workDate: string
  checkIn?: string
  checkOut?: string
  workingMinutes: number
  overtimeMinutes: number
  attendanceStatus: string
  source: string
  note?: string
  confirmedByName?: string
  version: number
}

export type HrLeave = {
  id: number
  employeeId: number
  employeeCode: string
  employeeName: string
  teamId: number
  teamName: string
  fromDate: string
  toDate: string
  leaveType: string
  reason: string
  status: string
  approvedByName?: string
  reviewComment?: string
  reviewedAt?: string
  createdAt: string
  version: number
}

export type HrKpi = {
  id: number
  employeeId: number
  employeeCode: string
  employeeName: string
  teamId: number
  teamName: string
  periodStart: string
  periodEnd: string
  score?: number
  productivityScore?: number
  qualityScore?: number
  attendanceScore?: number
  note?: string
  version: number
}

export type HrOvertime = {
  id: number
  employeeId?: number
  employeeCode?: string
  employeeName?: string
  teamId?: number
  teamName?: string
  workDate: string
  requestedMinutes: number
  reason: string
  status: string
  approvedByName?: string
  reviewComment?: string
  reviewedAt?: string
  createdAt: string
  version?: number
}

export type HrAssignment = {
  id: number
  employeeId: number
  employeeCode: string
  employeeName: string
  sourceTeamId?: number
  sourceTeamName?: string
  targetTeamId: number
  targetTeamName: string
  assignmentType: string
  effectiveFrom: string
  effectiveTo?: string
  reason: string
  approvedByName?: string
  active: boolean
  version: number
}

export type HrNotification = {
  id: number
  recipientEmployeeId: number
  recipientName: string
  title: string
  message: string
  severity: string
  actionUrl?: string
  read: boolean
  readAt?: string
  createdAt: string
}

export type MaintenancePriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

export type MaintenanceRequestStatus =
  | 'OPEN'
  | 'ACKNOWLEDGED'
  | 'IN_PROGRESS'
  | 'RESOLVED'
  | 'CANCELLED'

export type MaintenanceType = 'PREVENTIVE' | 'CORRECTIVE' | 'INSPECTION' | 'EMERGENCY'

export type MaintenanceWorkOrderStatus =
  | 'PLANNED'
  | 'ASSIGNED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED'

export type MachineOperationalStatus =
  | 'IDLE'
  | 'RUNNING'
  | 'STOPPED'
  | 'MAINTENANCE'
  | 'BREAKDOWN'

export type MaintenanceMachineOption = {
  id: number
  code: string
  name: string
  teamId: number
  teamName: string
  operationalStatus: MachineOperationalStatus
}

export type MaintenanceDowntimeOption = {
  id: number
  productionReportStagingId: number
  reportStatus: ProductionReportStatus
  machineId: number
  machineCode: string
  machineName: string
  downtimeReasonId: number
  downtimeReasonCode: string
  downtimeReasonName: string
  downtimeReasonType: 'PLANNED' | 'UNPLANNED'
  startTime: string
  endTime: string
  durationMinutes: number
  description?: string
  active: boolean
}

export type MaintenanceRequestItem = {
  id: number
  requestNo: string
  machineId: number
  machineCode: string
  machineName: string
  teamId: number
  teamName: string
  reportedById: number
  reportedByName: string
  sourceDowntimeStagingId?: number
  priority: MaintenancePriority
  status: MaintenanceRequestStatus
  title: string
  description: string
  impactDescription?: string
  reportedAt: string
  resolvedAt?: string
  resolutionNote?: string
  version: number
}

export type MaintenanceScheduleItem = {
  id: number
  machineId: number
  machineCode: string
  machineName: string
  teamId: number
  teamName: string
  maintenanceType: MaintenanceType
  name: string
  intervalDays: number
  lastCompletedDate?: string
  nextDueDate: string
  description?: string
  active: boolean
  version: number
}

export type MaintenancePartUsageItem = {
  id: number
  materialId: number
  materialCode: string
  materialName: string
  quantity: number
  unit: string
  unitCost: number
  totalCost: number
}

export type MaintenanceWorkOrderItem = {
  id: number
  workOrderNo: string
  maintenanceRequestId?: number
  maintenanceScheduleId?: number
  machineId: number
  machineCode: string
  machineName: string
  teamId: number
  teamName: string
  maintenanceType: MaintenanceType
  priority: MaintenancePriority
  status: MaintenanceWorkOrderStatus
  assignedEmployeeId?: number
  assignedEmployeeName?: string
  title: string
  description?: string
  plannedStart: string
  plannedEnd: string
  actualStart?: string
  actualEnd?: string
  laborCost: number
  partCost: number
  externalCost: number
  totalCost: number
  completionNote?: string
  parts: MaintenancePartUsageItem[]
  version: number
}

export type MaintenanceStatusHistoryItem = {
  id: number
  machineId: number
  previousStatus?: MachineOperationalStatus
  newStatus: MachineOperationalStatus
  sourceType: string
  sourceId?: number
  note?: string
  changedBy: string
  changedAt: string
}

export type MaintenanceDashboard = {
  openRequests: number
  criticalRequests: number
  overdueSchedules: number
  activeWorkOrders: number
  completedCost: number
}
