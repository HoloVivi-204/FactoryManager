import { type FormEvent, type ReactNode, useCallback, useEffect, useState } from 'react'
import {
  maintenanceApi,
  type MaintenanceDashboardFilters,
  type MaintenanceRequestFilters,
  type MaintenanceScheduleFilters,
  type MaintenanceWorkOrderFilters,
} from '../api/maintenanceApi'
import { DataTable, KpiCard, LoadingState, Panel, StatusBadge } from '../components/ui'
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
  Role,
} from '../types'
import { number } from '../utils/format'

export type MaintenanceView =
  | 'maintenance-dashboard'
  | 'maintenance-requests'
  | 'maintenance-schedules'
  | 'maintenance-work-orders'
  | 'maintenance-history'

type FormValues = Record<string, string>
type Option = { value: string; label: string }
type FieldSpec = {
  key: string
  label: string
  type?: 'text' | 'number' | 'date' | 'datetime-local' | 'textarea' | 'select'
  options?: Option[]
  required?: boolean
  min?: number
  step?: string
  placeholder?: string
  full?: boolean
  hint?: string
}

const managerRoles: Role[] = ['ADMIN', 'FACTORY_MANAGER', 'DEPARTMENT_MANAGER', 'PRODUCTION_MANAGER']
const requestCreatorRoles: Role[] = [...managerRoles, 'TEAM_LEADER']
const priorities: Option[] = [
  { value: 'LOW', label: 'Thấp' },
  { value: 'MEDIUM', label: 'Trung bình' },
  { value: 'HIGH', label: 'Cao' },
  { value: 'CRITICAL', label: 'Khẩn cấp' },
]
const requestStatuses: Option[] = [
  { value: 'OPEN', label: 'Mới tạo' },
  { value: 'ACKNOWLEDGED', label: 'Đã tiếp nhận' },
  { value: 'IN_PROGRESS', label: 'Đang xử lý' },
  { value: 'RESOLVED', label: 'Đã giải quyết' },
  { value: 'CANCELLED', label: 'Đã hủy' },
]
const maintenanceTypes: Option[] = [
  { value: 'PREVENTIVE', label: 'Phòng ngừa' },
  { value: 'CORRECTIVE', label: 'Khắc phục' },
  { value: 'INSPECTION', label: 'Kiểm tra' },
  { value: 'EMERGENCY', label: 'Khẩn cấp' },
]
const workOrderStatuses: Option[] = [
  { value: 'PLANNED', label: 'Đã lập kế hoạch' },
  { value: 'ASSIGNED', label: 'Đã phân công' },
  { value: 'IN_PROGRESS', label: 'Đang thực hiện' },
  { value: 'COMPLETED', label: 'Hoàn tất' },
  { value: 'CANCELLED', label: 'Đã hủy' },
]
const machineStatuses: Record<string, string> = {
  IDLE: 'Đang rảnh',
  RUNNING: 'Đang chạy',
  STOPPED: 'Đã dừng',
  MAINTENANCE: 'Đang bảo trì',
  BREAKDOWN: 'Hỏng máy',
}

const optionLabel = (options: Option[], value: unknown) =>
  options.find((option) => option.value === value)?.label ?? String(value ?? '—')

const numberValue = (value: string) => value === '' ? undefined : Number(value)
const hasFiniteNumber = (value: unknown): value is number =>
  typeof value === 'number' && Number.isFinite(value)
const currencyFormatter = new Intl.NumberFormat('vi-VN', {
  style: 'currency',
  currency: 'VND',
  maximumFractionDigits: 0,
})
const currency = (value: unknown) =>
  hasFiniteNumber(value) ? currencyFormatter.format(value) : '—'
const dateTime = (value: unknown) => value
  ? new Date(String(value)).toLocaleString('vi-VN')
  : '—'
const today = () => new Date().toISOString().slice(0, 10)
const monthStart = () => `${today().slice(0, 8)}01`
const nextDateStart = (value: string) => {
  if (!value) return ''
  const date = new Date(`${value}T00:00:00`)
  date.setDate(date.getDate() + 1)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}T00:00:00`
}

export default function MaintenancePage({ role, view }: { role: Role; view: MaintenanceView }) {
  const canManage = managerRoles.includes(role)
  const canCreateRequest = requestCreatorRoles.includes(role)

  if (view === 'maintenance-dashboard') return <MaintenanceDashboardView />
  if (view === 'maintenance-requests') {
    return <MaintenanceRequestsView canManage={canManage} canCreate={canCreateRequest} />
  }
  if (view === 'maintenance-schedules') return <MaintenanceSchedulesView canManage={canManage} />
  if (view === 'maintenance-work-orders') return <MaintenanceWorkOrdersView canManage={canManage} />
  return <MaintenanceHistoryView />
}

function MaintenanceDashboardView() {
  const defaults = { teamId: '', fromDate: monthStart(), toDate: today() }
  const [filters, setFilters] = useState(defaults)
  const [applied, setApplied] = useState(defaults)
  const [dashboard, setDashboard] = useState<MaintenanceDashboard>()
  const [orders, setOrders] = useState<PageResponse<MaintenanceWorkOrderItem>>()
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const dashboardFilters: MaintenanceDashboardFilters = {
        teamId: applied.teamId,
        fromDate: applied.fromDate,
        toDate: applied.toDate,
      }
      const orderFilters: MaintenanceWorkOrderFilters = {
        teamId: applied.teamId,
        status: 'COMPLETED',
        from: applied.fromDate ? `${applied.fromDate}T00:00:00` : '',
        to: nextDateStart(applied.toDate),
        page,
        size: 10,
      }
      const [summary, completed] = await Promise.all([
        maintenanceApi.dashboard(dashboardFilters),
        maintenanceApi.workOrders(orderFilters),
      ])
      setDashboard(summary)
      setOrders(completed)
    } catch (loadError) {
      setError((loadError as Error).message)
    } finally {
      setLoading(false)
    }
  }, [applied, page])

  useEffect(() => {
    void load()
  }, [load])

  const columns = [
    { key: 'workOrderNo', label: 'Mã phiếu' },
    { key: 'machineCode', label: 'Máy' },
    { key: 'teamName', label: 'Tổ' },
    { key: 'title', label: 'Nội dung' },
    { key: 'actualEnd', label: 'Hoàn tất', render: (row: MaintenanceWorkOrderItem) => dateTime(row.actualEnd) },
    { key: 'laborCost', label: 'Nhân công', render: (row: MaintenanceWorkOrderItem) => currency(row.laborCost) },
    { key: 'partCost', label: 'Vật tư', render: (row: MaintenanceWorkOrderItem) => currency(row.partCost) },
    { key: 'externalCost', label: 'Thuê ngoài', render: (row: MaintenanceWorkOrderItem) => currency(row.externalCost) },
    { key: 'totalCost', label: 'Tổng chi phí', render: (row: MaintenanceWorkOrderItem) => currency(row.totalCost) },
  ]

  return (
    <>
      <PageTitle
        title="Dashboard bảo trì"
        description="Số liệu tổng hợp và chi phí bảo trì trong đúng phạm vi được cấp bởi JWT."
      />
      <Panel title="Bộ lọc phạm vi và thời gian">
        <div className="filters hr-filters">
          <FilterInput label="ID tổ" type="number" value={filters.teamId} onChange={(value) => setFilters({ ...filters, teamId: value })} />
          <FilterInput label="Từ ngày" type="date" value={filters.fromDate} onChange={(value) => setFilters({ ...filters, fromDate: value })} />
          <FilterInput label="Đến ngày" type="date" value={filters.toDate} onChange={(value) => setFilters({ ...filters, toDate: value })} />
          <button onClick={() => { setPage(0); setApplied({ ...filters }) }}>Áp dụng</button>
        </div>
      </Panel>
      <LoadingState loading={loading} error={error} />
      <div className="kpi-grid maintenance-kpis">
        <KpiCard label="Yêu cầu đang mở" value={number(dashboard?.openRequests)} hint="Chưa kết thúc" tone="blue" />
        <KpiCard label="Yêu cầu khẩn cấp" value={number(dashboard?.criticalRequests)} hint="Cần ưu tiên" tone="orange" />
        <KpiCard label="Lịch đã quá hạn" value={number(dashboard?.overdueSchedules)} hint="Đang hoạt động" tone="purple" />
        <KpiCard label="Phiếu đang thực hiện" value={number(dashboard?.activeWorkOrders)} hint="Đã giao hoặc đang làm" tone="green" />
        <KpiCard label="Chi phí đã hoàn tất" value={currency(dashboard?.completedCost)} hint="Theo khoảng ngày đã chọn" tone="orange" />
      </div>
      <Panel title={`Chi tiết chi phí phiếu đã hoàn tất (${number(orders?.totalElements)})`}>
        <DataTable rows={orders?.content ?? []} columns={columns} />
        <Pager result={orders} page={page} onPage={setPage} />
      </Panel>
    </>
  )
}

function MaintenanceRequestsView({ canManage, canCreate }: { canManage: boolean; canCreate: boolean }) {
  const defaults = { machineId: '', teamId: '', status: '', priority: '', keyword: '', fromDate: '', toDate: '' }
  const [filters, setFilters] = useState(defaults)
  const [applied, setApplied] = useState(defaults)
  const [result, setResult] = useState<PageResponse<MaintenanceRequestItem>>()
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)
  const [createOpen, setCreateOpen] = useState(false)
  const [createForm, setCreateForm] = useState<FormValues>({ priority: 'MEDIUM' })
  const [statusTarget, setStatusTarget] = useState<MaintenanceRequestItem>()
  const [statusForm, setStatusForm] = useState<FormValues>({})
  const [detail, setDetail] = useState<MaintenanceRequestItem>()
  const [busy, setBusy] = useState(false)
  const [modalError, setModalError] = useState('')
  const [machineOptions, setMachineOptions] = useState<MaintenanceMachineOption[]>([])
  const [machineOptionsLoading, setMachineOptionsLoading] = useState(false)
  const [machineOptionsError, setMachineOptionsError] = useState('')
  const [downtimeOptions, setDowntimeOptions] = useState<MaintenanceDowntimeOption[]>([])
  const [downtimeOptionsLoading, setDowntimeOptionsLoading] = useState(false)
  const [downtimeOptionsError, setDowntimeOptionsError] = useState('')

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const request: MaintenanceRequestFilters = {
        machineId: applied.machineId,
        teamId: applied.teamId,
        status: applied.status as MaintenanceRequestStatus | '',
        priority: applied.priority as MaintenancePriority | '',
        keyword: applied.keyword,
        fromDate: applied.fromDate,
        toDate: applied.toDate,
        page,
        size: 25,
      }
      setResult(await maintenanceApi.requests(request))
    } catch (loadError) {
      setError((loadError as Error).message)
    } finally {
      setLoading(false)
    }
  }, [applied, page])

  useEffect(() => {
    void load()
  }, [load, reloadKey])

  useEffect(() => {
    if (!canCreate) return
    let cancelled = false
    setMachineOptionsLoading(true)
    setMachineOptionsError('')
    maintenanceApi.machineOptions()
      .then((items) => {
        if (!cancelled) setMachineOptions(items)
      })
      .catch((loadError) => {
        if (!cancelled) setMachineOptionsError((loadError as Error).message)
      })
      .finally(() => {
        if (!cancelled) setMachineOptionsLoading(false)
      })
    return () => { cancelled = true }
  }, [canCreate])

  useEffect(() => {
    const machineId = Number(createForm.machineId)
    setDowntimeOptions([])
    setDowntimeOptionsError('')
    if (!createOpen || machineId <= 0) {
      setDowntimeOptionsLoading(false)
      return
    }

    let cancelled = false
    setDowntimeOptionsLoading(true)
    maintenanceApi.downtimesByMachine(machineId)
      .then((items) => {
        if (!cancelled) setDowntimeOptions(items)
      })
      .catch((loadError) => {
        if (!cancelled) setDowntimeOptionsError((loadError as Error).message)
      })
      .finally(() => {
        if (!cancelled) setDowntimeOptionsLoading(false)
      })
    return () => { cancelled = true }
  }, [createOpen, createForm.machineId])

  function openCreate() {
    setCreateForm({
      priority: 'MEDIUM',
      machineId: machineOptions.length === 1 ? String(machineOptions[0].id) : '',
      sourceDowntimeStagingId: '',
    })
    setModalError('')
    setCreateOpen(true)
  }

  function changeCreateForm(values: FormValues) {
    if (values.machineId !== createForm.machineId) {
      setCreateForm({ ...values, sourceDowntimeStagingId: '' })
      return
    }
    setCreateForm(values)
  }

  async function createRequest(event: FormEvent) {
    event.preventDefault()
    setBusy(true)
    setModalError('')
    try {
      await maintenanceApi.createRequest({
        machineId: Number(createForm.machineId),
        sourceDowntimeStagingId: numberValue(createForm.sourceDowntimeStagingId ?? ''),
        priority: createForm.priority,
        title: createForm.title,
        description: createForm.description,
        impactDescription: createForm.impactDescription || undefined,
      })
      setCreateOpen(false)
      setReloadKey((value) => value + 1)
    } catch (saveError) {
      setModalError((saveError as Error).message)
    } finally {
      setBusy(false)
    }
  }

  function openStatus(row: MaintenanceRequestItem) {
    const options = nextRequestStatuses(row.status)
    setStatusTarget(row)
    setStatusForm({ status: options[0]?.value ?? '', resolutionNote: '' })
    setModalError('')
  }

  async function updateStatus(event: FormEvent) {
    event.preventDefault()
    if (!statusTarget) return
    setBusy(true)
    setModalError('')
    try {
      await maintenanceApi.updateRequestStatus(statusTarget.id, {
        status: statusForm.status,
        resolutionNote: statusForm.resolutionNote || undefined,
      })
      setStatusTarget(undefined)
      setReloadKey((value) => value + 1)
    } catch (saveError) {
      setModalError((saveError as Error).message)
    } finally {
      setBusy(false)
    }
  }

  async function openDetail(row: MaintenanceRequestItem) {
    setError('')
    try {
      setDetail(await maintenanceApi.request(row.id))
    } catch (detailError) {
      setError((detailError as Error).message)
    }
  }

  const columns = [
    { key: 'requestNo', label: 'Mã yêu cầu' },
    { key: 'reportedAt', label: 'Thời điểm', render: (row: MaintenanceRequestItem) => dateTime(row.reportedAt) },
    { key: 'machineCode', label: 'Máy' },
    { key: 'teamName', label: 'Tổ' },
    { key: 'title', label: 'Nội dung' },
    { key: 'reportedByName', label: 'Người báo' },
    { key: 'priority', label: 'Ưu tiên', render: (row: MaintenanceRequestItem) => <StatusText value={row.priority} label={optionLabel(priorities, row.priority)} /> },
    { key: 'status', label: 'Trạng thái', render: (row: MaintenanceRequestItem) => <StatusText value={row.status} label={optionLabel(requestStatuses, row.status)} /> },
    {
      key: 'actions', label: 'Thao tác', render: (row: MaintenanceRequestItem) => (
        <div className="admin-actions">
          <button onClick={() => void openDetail(row)}>Chi tiết</button>
          {canManage && nextRequestStatuses(row.status).length > 0 && <button onClick={() => openStatus(row)}>Xử lý</button>}
        </div>
      ),
    },
  ]

  const createFields: FieldSpec[] = [
    {
      key: 'machineId',
      label: 'Máy cần bảo trì',
      type: 'select',
      options: machineOptions.map((machine) => ({
        value: String(machine.id),
        label: `${machine.code} · ${machine.name} · ${machine.teamName} (${machineStatuses[machine.operationalStatus] ?? machine.operationalStatus})`,
      })),
      required: true,
      hint: machineOptionsLoading
        ? 'Đang tải danh sách máy trong phạm vi phụ trách…'
        : machineOptions.length === 0
          ? 'Không có máy đang hoạt động trong phạm vi của tài khoản.'
          : 'Danh sách chỉ gồm máy thuộc phạm vi được phân quyền.',
    },
    {
      key: 'sourceDowntimeStagingId',
      label: 'Lần dừng máy liên quan',
      type: 'select',
      options: downtimeOptions.map((downtime) => ({
        value: String(downtime.id),
        label: `${dateTime(downtime.startTime)} · ${downtime.downtimeReasonName} · ${downtime.durationMinutes} phút · BC #${downtime.productionReportStagingId} (${downtime.reportStatus})`,
      })),
      hint: !createForm.machineId
        ? 'Chọn máy trước; có thể để trống nếu yêu cầu không phát sinh từ một lần dừng máy.'
        : downtimeOptionsLoading
          ? 'Đang tải các lần dừng của máy…'
          : downtimeOptions.length === 0
            ? 'Máy này chưa có dữ liệu dừng máy; bạn có thể để trống.'
            : 'Không bắt buộc. Chỉ chọn khi yêu cầu phát sinh từ một lần dừng máy đã ghi nhận.',
    },
    { key: 'priority', label: 'Mức ưu tiên', type: 'select', options: priorities, required: true },
    { key: 'title', label: 'Tiêu đề', required: true },
    { key: 'description', label: 'Mô tả sự cố', type: 'textarea', required: true, full: true },
    { key: 'impactDescription', label: 'Ảnh hưởng đến sản xuất', type: 'textarea', full: true },
  ]

  return (
    <>
      <PageTitle
        title="Yêu cầu bảo trì"
        description={canManage ? 'Tiếp nhận, phân loại và xử lý yêu cầu trong phạm vi quản lý.' : 'Gửi và theo dõi yêu cầu sửa chữa máy thuộc tổ phụ trách.'}
        action={canCreate && <button className="admin-add-button" onClick={openCreate}>+ Tạo yêu cầu</button>}
      />
      <Panel title="Bộ lọc yêu cầu">
        <div className="filters hr-filters">
          <label>Từ ngày<input type="date" value={filters.fromDate} onChange={(event) => setFilters({ ...filters, fromDate: event.target.value })} /></label>
          <label>Đến ngày<input type="date" value={filters.toDate} onChange={(event) => setFilters({ ...filters, toDate: event.target.value })} /></label>
          <FilterSelect label="Máy" value={filters.machineId} options={machineOptions.map((machine) => ({ value: String(machine.id), label: `${machine.code} - ${machine.name} - ${machine.teamName}` }))} onChange={(value) => setFilters({ ...filters, machineId: value })} />
          <FilterSelect label="Trạng thái" value={filters.status} options={requestStatuses} onChange={(value) => setFilters({ ...filters, status: value })} />
          <FilterSelect label="Ưu tiên" value={filters.priority} options={priorities} onChange={(value) => setFilters({ ...filters, priority: value })} />
          <label>Từ khóa<input type="search" placeholder="Mã yêu cầu, máy, nội dung, người báo…" value={filters.keyword} onChange={(event) => setFilters({ ...filters, keyword: event.target.value })} /></label>
          <button onClick={() => { setPage(0); setApplied({ ...filters }) }}>Áp dụng</button>
          <button type="button" onClick={() => { setFilters(defaults); setApplied(defaults); setPage(0) }}>Xóa bộ lọc</button>
        </div>
      </Panel>
      <Panel title={`${number(result?.totalElements)} yêu cầu`}>
        <LoadingState loading={loading} error={error} />
        <DataTable rows={result?.content ?? []} columns={columns} />
        <Pager result={result} page={page} onPage={setPage} />
      </Panel>

      {createOpen && (
        <EditorModal title="Tạo yêu cầu bảo trì" subtitle="Yêu cầu mới luôn bắt đầu ở trạng thái Mới tạo." fields={createFields} values={createForm} onChange={changeCreateForm} busy={busy} error={[modalError, machineOptionsError, downtimeOptionsError].filter(Boolean).join(' ')} onClose={() => setCreateOpen(false)} onSubmit={createRequest} />
      )}
      {statusTarget && (
        <EditorModal
          title={`Xử lý ${statusTarget.requestNo}`}
          subtitle={`Hiện tại: ${optionLabel(requestStatuses, statusTarget.status)}`}
          fields={[
            { key: 'status', label: 'Chuyển sang', type: 'select', options: nextRequestStatuses(statusTarget.status), required: true },
            { key: 'resolutionNote', label: statusForm.status === 'RESOLVED' ? 'Kết quả xử lý' : 'Ghi chú xử lý', type: 'textarea', required: statusForm.status === 'RESOLVED', full: true },
          ]}
          values={statusForm}
          onChange={setStatusForm}
          busy={busy}
          error={modalError}
          onClose={() => setStatusTarget(undefined)}
          onSubmit={updateStatus}
        />
      )}
      {detail && (
        <DetailModal title={detail.requestNo} subtitle={detail.title} onClose={() => setDetail(undefined)}>
          <DetailGrid values={[
            ['Máy', `${detail.machineCode} · ${detail.machineName}`],
            ['Tổ', detail.teamName],
            ['Người báo', detail.reportedByName],
            ['Thời điểm báo', dateTime(detail.reportedAt)],
            ['Ưu tiên', optionLabel(priorities, detail.priority)],
            ['Trạng thái', optionLabel(requestStatuses, detail.status)],
            ['Downtime nguồn', detail.sourceDowntimeStagingId ?? '—'],
            ['Đã giải quyết lúc', dateTime(detail.resolvedAt)],
          ]} />
          <DetailText label="Mô tả" value={detail.description} />
          <DetailText label="Ảnh hưởng" value={detail.impactDescription} />
          <DetailText label="Kết quả xử lý" value={detail.resolutionNote} />
        </DetailModal>
      )}
    </>
  )
}

function MaintenanceSchedulesView({ canManage }: { canManage: boolean }) {
  const defaults = { machineId: '', teamId: '', active: 'true', dueBefore: '' }
  const [filters, setFilters] = useState(defaults)
  const [applied, setApplied] = useState(defaults)
  const [result, setResult] = useState<PageResponse<MaintenanceScheduleItem>>()
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)
  const [editing, setEditing] = useState<MaintenanceScheduleItem | null | undefined>(undefined)
  const [form, setForm] = useState<FormValues>({})
  const [busy, setBusy] = useState(false)
  const [modalError, setModalError] = useState('')

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const request: MaintenanceScheduleFilters = {
        machineId: applied.machineId,
        teamId: applied.teamId,
        active: applied.active === '' ? '' : applied.active === 'true',
        dueBefore: applied.dueBefore,
        page,
        size: 25,
      }
      setResult(await maintenanceApi.schedules(request))
    } catch (loadError) {
      setError((loadError as Error).message)
    } finally {
      setLoading(false)
    }
  }, [applied, page])

  useEffect(() => {
    void load()
  }, [load, reloadKey])

  function openCreate() {
    setEditing(null)
    setForm({ maintenanceType: 'PREVENTIVE', intervalDays: '30', nextDueDate: today(), active: 'true' })
    setModalError('')
  }

  async function openEdit(row: MaintenanceScheduleItem) {
    setModalError('')
    try {
      const current = await maintenanceApi.schedule(row.id)
      setEditing(current)
      setForm({
        machineId: String(current.machineId),
        maintenanceType: current.maintenanceType,
        name: current.name,
        intervalDays: String(current.intervalDays),
        lastCompletedDate: current.lastCompletedDate ?? '',
        nextDueDate: current.nextDueDate,
        description: current.description ?? '',
        active: String(current.active),
      })
    } catch (editError) {
      setError((editError as Error).message)
    }
  }

  async function save(event: FormEvent) {
    event.preventDefault()
    setBusy(true)
    setModalError('')
    try {
      const body = {
        machineId: Number(form.machineId),
        maintenanceType: form.maintenanceType,
        name: form.name,
        intervalDays: Number(form.intervalDays),
        lastCompletedDate: form.lastCompletedDate || undefined,
        nextDueDate: form.nextDueDate,
        description: form.description || undefined,
        active: form.active !== 'false',
      }
      if (editing?.id) await maintenanceApi.updateSchedule(editing.id, body)
      else await maintenanceApi.createSchedule(body)
      setEditing(undefined)
      setReloadKey((value) => value + 1)
    } catch (saveError) {
      setModalError((saveError as Error).message)
    } finally {
      setBusy(false)
    }
  }

  async function deactivate(row: MaintenanceScheduleItem) {
    if (!confirm(`Ngừng lịch "${row.name}"? Dữ liệu lịch vẫn được giữ lại.`)) return
    setBusy(true)
    try {
      await maintenanceApi.deleteSchedule(row.id)
      setReloadKey((value) => value + 1)
    } catch (deleteError) {
      setError((deleteError as Error).message)
    } finally {
      setBusy(false)
    }
  }

  const columns = [
    { key: 'machineCode', label: 'Máy' },
    { key: 'teamName', label: 'Tổ' },
    { key: 'name', label: 'Tên lịch' },
    { key: 'maintenanceType', label: 'Loại', render: (row: MaintenanceScheduleItem) => optionLabel(maintenanceTypes, row.maintenanceType) },
    { key: 'intervalDays', label: 'Chu kỳ', render: (row: MaintenanceScheduleItem) => `${row.intervalDays} ngày` },
    { key: 'lastCompletedDate', label: 'Lần gần nhất' },
    { key: 'nextDueDate', label: 'Hạn tiếp theo' },
    { key: 'active', label: 'Trạng thái', render: (row: MaintenanceScheduleItem) => <StatusText value={row.active} label={row.active ? 'Đang dùng' : 'Đã ngừng'} /> },
    ...(canManage ? [{
      key: 'actions', label: 'Thao tác', render: (row: MaintenanceScheduleItem) => (
        <div className="admin-actions">
          <button onClick={() => void openEdit(row)}>Sửa</button>
          {row.active && <button className="danger-link" disabled={busy} onClick={() => void deactivate(row)}>Ngừng</button>}
        </div>
      ),
    }] : []),
  ]

  const fields: FieldSpec[] = [
    { key: 'machineId', label: 'ID máy', type: 'number', min: 1, required: true },
    { key: 'maintenanceType', label: 'Loại bảo trì', type: 'select', options: maintenanceTypes, required: true },
    { key: 'name', label: 'Tên lịch', required: true },
    { key: 'intervalDays', label: 'Chu kỳ (ngày)', type: 'number', min: 1, required: true },
    { key: 'lastCompletedDate', label: 'Ngày hoàn thành gần nhất', type: 'date' },
    { key: 'nextDueDate', label: 'Hạn bảo trì tiếp theo', type: 'date', required: true },
    { key: 'active', label: 'Trạng thái', type: 'select', options: [{ value: 'true', label: 'Đang sử dụng' }, { value: 'false', label: 'Ngừng sử dụng' }], required: true },
    { key: 'description', label: 'Mô tả', type: 'textarea', full: true },
  ]

  return (
    <>
      <PageTitle title="Lịch bảo trì định kỳ" description="Lập và theo dõi lịch bảo trì theo máy trong phạm vi quản lý." action={canManage && <button className="admin-add-button" onClick={openCreate}>+ Tạo lịch</button>} />
      <Panel title="Bộ lọc lịch">
        <div className="filters hr-filters">
          <FilterInput label="ID máy" type="number" value={filters.machineId} onChange={(value) => setFilters({ ...filters, machineId: value })} />
          <FilterInput label="ID tổ" type="number" value={filters.teamId} onChange={(value) => setFilters({ ...filters, teamId: value })} />
          <FilterSelect label="Trạng thái" value={filters.active} options={[{ value: 'true', label: 'Đang dùng' }, { value: 'false', label: 'Đã ngừng' }]} onChange={(value) => setFilters({ ...filters, active: value })} />
          <FilterInput label="Đến hạn trước" type="date" value={filters.dueBefore} onChange={(value) => setFilters({ ...filters, dueBefore: value })} />
          <button onClick={() => { setPage(0); setApplied({ ...filters }) }}>Áp dụng</button>
        </div>
      </Panel>
      <Panel title={`${number(result?.totalElements)} lịch bảo trì`}>
        <LoadingState loading={loading} error={error} />
        <DataTable rows={result?.content ?? []} columns={columns} />
        <Pager result={result} page={page} onPage={setPage} />
      </Panel>
      {editing !== undefined && (
        <EditorModal title={editing ? `Cập nhật lịch #${editing.id}` : 'Tạo lịch bảo trì'} subtitle="Ngày hoàn thành gần nhất không được sau hạn tiếp theo." fields={fields} values={form} onChange={setForm} busy={busy} error={modalError} onClose={() => setEditing(undefined)} onSubmit={save} />
      )}
    </>
  )
}

function MaintenanceWorkOrdersView({ canManage }: { canManage: boolean }) {
  const defaults = { machineId: '', teamId: '', status: '', priority: '', from: '', to: '' }
  const [filters, setFilters] = useState(defaults)
  const [applied, setApplied] = useState(defaults)
  const [result, setResult] = useState<PageResponse<MaintenanceWorkOrderItem>>()
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)
  const [createOpen, setCreateOpen] = useState(false)
  const [form, setForm] = useState<FormValues>({})
  const [statusTarget, setStatusTarget] = useState<MaintenanceWorkOrderItem>()
  const [statusForm, setStatusForm] = useState<FormValues>({})
  const [detail, setDetail] = useState<MaintenanceWorkOrderItem>()
  const [partForm, setPartForm] = useState<FormValues>({})
  const [busy, setBusy] = useState(false)
  const [modalError, setModalError] = useState('')

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const request: MaintenanceWorkOrderFilters = {
        machineId: applied.machineId,
        teamId: applied.teamId,
        status: applied.status as MaintenanceWorkOrderStatus | '',
        priority: applied.priority as MaintenancePriority | '',
        from: applied.from ? `${applied.from}T00:00:00` : '',
        to: nextDateStart(applied.to),
        page,
        size: 25,
      }
      setResult(await maintenanceApi.workOrders(request))
    } catch (loadError) {
      setError((loadError as Error).message)
    } finally {
      setLoading(false)
    }
  }, [applied, page])

  useEffect(() => {
    void load()
  }, [load, reloadKey])

  function openCreate() {
    const now = new Date()
    const end = new Date(now.getTime() + 60 * 60 * 1000)
    const local = (value: Date) => {
      const offset = value.getTimezoneOffset() * 60_000
      return new Date(value.getTime() - offset).toISOString().slice(0, 16)
    }
    setForm({
      maintenanceType: 'CORRECTIVE',
      priority: 'MEDIUM',
      plannedStart: local(now),
      plannedEnd: local(end),
      laborCost: '0',
      externalCost: '0',
    })
    setModalError('')
    setCreateOpen(true)
  }

  async function create(event: FormEvent) {
    event.preventDefault()
    if (!form.maintenanceRequestId && !form.maintenanceScheduleId) {
      setModalError('Cần nhập ID yêu cầu bảo trì hoặc ID lịch bảo trì làm nguồn.')
      return
    }
    setBusy(true)
    setModalError('')
    try {
      await maintenanceApi.createWorkOrder({
        maintenanceRequestId: numberValue(form.maintenanceRequestId ?? ''),
        maintenanceScheduleId: numberValue(form.maintenanceScheduleId ?? ''),
        machineId: Number(form.machineId),
        maintenanceType: form.maintenanceType,
        priority: form.priority,
        assignedEmployeeId: numberValue(form.assignedEmployeeId ?? ''),
        title: form.title,
        description: form.description || undefined,
        plannedStart: form.plannedStart,
        plannedEnd: form.plannedEnd,
        laborCost: Number(form.laborCost || 0),
        externalCost: Number(form.externalCost || 0),
      })
      setCreateOpen(false)
      setReloadKey((value) => value + 1)
    } catch (saveError) {
      setModalError((saveError as Error).message)
    } finally {
      setBusy(false)
    }
  }

  async function openDetail(row: MaintenanceWorkOrderItem) {
    setError('')
    setPartForm({})
    setModalError('')
    try {
      setDetail(await maintenanceApi.workOrder(row.id))
    } catch (detailError) {
      setError((detailError as Error).message)
    }
  }

  function openStatus(row: MaintenanceWorkOrderItem) {
    const options = nextWorkOrderStatuses(row.status)
    setStatusTarget(row)
    setStatusForm({ status: options[0]?.value ?? '', actualStart: '', actualEnd: '', completionNote: '' })
    setModalError('')
  }

  async function updateStatus(event: FormEvent) {
    event.preventDefault()
    if (!statusTarget) return
    setBusy(true)
    setModalError('')
    try {
      const updated = await maintenanceApi.updateWorkOrderStatus(statusTarget.id, {
        status: statusForm.status,
        actualStart: statusForm.actualStart || undefined,
        actualEnd: statusForm.actualEnd || undefined,
        completionNote: statusForm.completionNote || undefined,
      })
      if (detail?.id === updated.id) setDetail(updated)
      setStatusTarget(undefined)
      setReloadKey((value) => value + 1)
    } catch (saveError) {
      setModalError((saveError as Error).message)
    } finally {
      setBusy(false)
    }
  }

  async function addPart(event: FormEvent) {
    event.preventDefault()
    if (!detail) return
    setBusy(true)
    setModalError('')
    try {
      await maintenanceApi.addPart(detail.id, {
        materialId: Number(partForm.materialId),
        quantity: Number(partForm.quantity),
        unitCost: Number(partForm.unitCost),
      })
      setDetail(await maintenanceApi.workOrder(detail.id))
      setPartForm({})
      setReloadKey((value) => value + 1)
    } catch (partError) {
      setModalError((partError as Error).message)
    } finally {
      setBusy(false)
    }
  }

  async function deletePart(part: MaintenancePartUsageItem) {
    if (!detail || !confirm(`Bỏ vật tư ${part.materialCode} khỏi phiếu?`)) return
    setBusy(true)
    setModalError('')
    try {
      await maintenanceApi.deletePart(detail.id, part.id)
      setDetail(await maintenanceApi.workOrder(detail.id))
      setReloadKey((value) => value + 1)
    } catch (partError) {
      setModalError((partError as Error).message)
    } finally {
      setBusy(false)
    }
  }

  const columns = [
    { key: 'workOrderNo', label: 'Mã phiếu' },
    { key: 'plannedStart', label: 'Dự kiến bắt đầu', render: (row: MaintenanceWorkOrderItem) => dateTime(row.plannedStart) },
    { key: 'machineCode', label: 'Máy' },
    { key: 'teamName', label: 'Tổ' },
    { key: 'title', label: 'Công việc' },
    { key: 'assignedEmployeeName', label: 'Phụ trách' },
    { key: 'priority', label: 'Ưu tiên', render: (row: MaintenanceWorkOrderItem) => <StatusText value={row.priority} label={optionLabel(priorities, row.priority)} /> },
    { key: 'status', label: 'Trạng thái', render: (row: MaintenanceWorkOrderItem) => <StatusText value={row.status} label={optionLabel(workOrderStatuses, row.status)} /> },
    { key: 'totalCost', label: 'Chi phí', render: (row: MaintenanceWorkOrderItem) => currency(row.totalCost) },
    {
      key: 'actions', label: 'Thao tác', render: (row: MaintenanceWorkOrderItem) => (
        <div className="admin-actions">
          <button onClick={() => void openDetail(row)}>Chi tiết</button>
          {canManage && nextWorkOrderStatuses(row.status).length > 0 && <button onClick={() => openStatus(row)}>Cập nhật</button>}
        </div>
      ),
    },
  ]

  const createFields: FieldSpec[] = [
    { key: 'maintenanceRequestId', label: 'ID yêu cầu nguồn', type: 'number', min: 1, hint: 'Nhập một trong hai nguồn.' },
    { key: 'maintenanceScheduleId', label: 'ID lịch nguồn', type: 'number', min: 1, hint: 'Phải cùng máy với phiếu.' },
    { key: 'machineId', label: 'ID máy', type: 'number', min: 1, required: true },
    { key: 'assignedEmployeeId', label: 'ID nhân viên phụ trách', type: 'number', min: 1 },
    { key: 'maintenanceType', label: 'Loại bảo trì', type: 'select', options: maintenanceTypes, required: true },
    { key: 'priority', label: 'Mức ưu tiên', type: 'select', options: priorities, required: true },
    { key: 'title', label: 'Tên công việc', required: true },
    { key: 'description', label: 'Mô tả', type: 'textarea', full: true },
    { key: 'plannedStart', label: 'Bắt đầu dự kiến', type: 'datetime-local', required: true },
    { key: 'plannedEnd', label: 'Kết thúc dự kiến', type: 'datetime-local', required: true },
    { key: 'laborCost', label: 'Chi phí nhân công', type: 'number', min: 0, step: '0.01' },
    { key: 'externalCost', label: 'Chi phí thuê ngoài', type: 'number', min: 0, step: '0.01' },
  ]

  return (
    <>
      <PageTitle title="Phiếu công việc bảo trì" description="Quản lý thực hiện, thời gian, người phụ trách, vật tư và chi phí từng công việc." action={canManage && <button className="admin-add-button" onClick={openCreate}>+ Lập phiếu</button>} />
      <Panel title="Bộ lọc phiếu công việc">
        <div className="filters hr-filters">
          <FilterInput label="ID máy" type="number" value={filters.machineId} onChange={(value) => setFilters({ ...filters, machineId: value })} />
          <FilterInput label="ID tổ" type="number" value={filters.teamId} onChange={(value) => setFilters({ ...filters, teamId: value })} />
          <FilterSelect label="Trạng thái" value={filters.status} options={workOrderStatuses} onChange={(value) => setFilters({ ...filters, status: value })} />
          <FilterSelect label="Ưu tiên" value={filters.priority} options={priorities} onChange={(value) => setFilters({ ...filters, priority: value })} />
          <FilterInput label="Từ ngày" type="date" value={filters.from} onChange={(value) => setFilters({ ...filters, from: value })} />
          <FilterInput label="Đến ngày" type="date" value={filters.to} onChange={(value) => setFilters({ ...filters, to: value })} />
          <button onClick={() => { setPage(0); setApplied({ ...filters }) }}>Áp dụng</button>
        </div>
      </Panel>
      <Panel title={`${number(result?.totalElements)} phiếu công việc`}>
        <LoadingState loading={loading} error={error} />
        <DataTable rows={result?.content ?? []} columns={columns} />
        <Pager result={result} page={page} onPage={setPage} />
      </Panel>

      {createOpen && <EditorModal title="Lập phiếu công việc" subtitle="Phiếu phải liên kết ít nhất một yêu cầu hoặc lịch bảo trì đang hiệu lực." fields={createFields} values={form} onChange={setForm} busy={busy} error={modalError} onClose={() => setCreateOpen(false)} onSubmit={create} />}
      {statusTarget && (
        <EditorModal
          title={`Cập nhật ${statusTarget.workOrderNo}`}
          subtitle={`Hiện tại: ${optionLabel(workOrderStatuses, statusTarget.status)}`}
          fields={[
            { key: 'status', label: 'Chuyển sang', type: 'select', options: nextWorkOrderStatuses(statusTarget.status), required: true },
            { key: 'actualStart', label: 'Bắt đầu thực tế', type: 'datetime-local', hint: 'Để trống khi bắt đầu để hệ thống lấy thời điểm hiện tại.' },
            { key: 'actualEnd', label: 'Kết thúc thực tế', type: 'datetime-local', hint: 'Để trống khi hoàn tất để hệ thống lấy thời điểm hiện tại.' },
            { key: 'completionNote', label: statusForm.status === 'COMPLETED' ? 'Kết quả hoàn thành' : 'Ghi chú', type: 'textarea', required: statusForm.status === 'COMPLETED', full: true },
          ]}
          values={statusForm}
          onChange={setStatusForm}
          busy={busy}
          error={modalError}
          onClose={() => setStatusTarget(undefined)}
          onSubmit={updateStatus}
        />
      )}
      {detail && (
        <DetailModal title={detail.workOrderNo} subtitle={detail.title} onClose={() => setDetail(undefined)} wide>
          <DetailGrid values={[
            ['Máy', `${detail.machineCode} · ${detail.machineName}`],
            ['Tổ', detail.teamName],
            ['Loại', optionLabel(maintenanceTypes, detail.maintenanceType)],
            ['Ưu tiên', optionLabel(priorities, detail.priority)],
            ['Trạng thái', optionLabel(workOrderStatuses, detail.status)],
            ['Người phụ trách', detail.assignedEmployeeName ?? 'Chưa phân công'],
            ['Yêu cầu nguồn', detail.maintenanceRequestId ?? '—'],
            ['Lịch nguồn', detail.maintenanceScheduleId ?? '—'],
            ['Dự kiến', `${dateTime(detail.plannedStart)} — ${dateTime(detail.plannedEnd)}`],
            ['Thực tế', `${dateTime(detail.actualStart)} — ${dateTime(detail.actualEnd)}`],
          ]} />
          <DetailText label="Mô tả" value={detail.description} />
          <DetailText label="Kết quả" value={detail.completionNote} />
          <div className="maintenance-cost-grid">
            <span>Nhân công<b>{currency(detail.laborCost)}</b></span>
            <span>Vật tư<b>{currency(detail.partCost)}</b></span>
            <span>Thuê ngoài<b>{currency(detail.externalCost)}</b></span>
            <span>Tổng cộng<b>{currency(detail.totalCost)}</b></span>
          </div>
          <div className="maintenance-parts-heading"><h3>Vật tư đã sử dụng</h3></div>
          <DataTable rows={detail.parts ?? []} columns={[
            { key: 'materialCode', label: 'Mã vật tư' },
            { key: 'materialName', label: 'Tên vật tư' },
            { key: 'quantity', label: 'Số lượng' },
            { key: 'unit', label: 'Đơn vị' },
            { key: 'unitCost', label: 'Đơn giá', render: (row: MaintenancePartUsageItem) => currency(row.unitCost) },
            { key: 'totalCost', label: 'Thành tiền', render: (row: MaintenancePartUsageItem) => currency(row.totalCost) },
            ...(canManage && !isTerminalWorkOrder(detail.status) ? [{ key: 'actions', label: 'Thao tác', render: (row: MaintenancePartUsageItem) => <button className="danger-link" disabled={busy} onClick={() => void deletePart(row)}>Bỏ</button> }] : []),
          ]} />
          {canManage && !isTerminalWorkOrder(detail.status) && (
            <form className="maintenance-part-form" onSubmit={addPart}>
              <Field field={{ key: 'materialId', label: 'ID vật tư', type: 'number', min: 1, required: true }} value={partForm.materialId ?? ''} onChange={(value) => setPartForm({ ...partForm, materialId: value })} />
              <Field field={{ key: 'quantity', label: 'Số lượng', type: 'number', min: 0.001, step: '0.001', required: true }} value={partForm.quantity ?? ''} onChange={(value) => setPartForm({ ...partForm, quantity: value })} />
              <Field field={{ key: 'unitCost', label: 'Đơn giá', type: 'number', min: 0, step: '0.01', required: true }} value={partForm.unitCost ?? ''} onChange={(value) => setPartForm({ ...partForm, unitCost: value })} />
              <button className="admin-add-button" disabled={busy} type="submit">+ Thêm vật tư</button>
            </form>
          )}
          {modalError && <p className="form-message error">{modalError}</p>}
        </DetailModal>
      )}
    </>
  )
}

function MaintenanceHistoryView() {
  const [machineId, setMachineId] = useState('')
  const [appliedMachineId, setAppliedMachineId] = useState<number>()
  const [result, setResult] = useState<PageResponse<MaintenanceStatusHistoryItem>>()
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    if (!appliedMachineId) return
    setLoading(true)
    setError('')
    try {
      setResult(await maintenanceApi.machineHistory(appliedMachineId, page, 25))
    } catch (loadError) {
      setError((loadError as Error).message)
    } finally {
      setLoading(false)
    }
  }, [appliedMachineId, page])

  useEffect(() => {
    if (appliedMachineId) void load()
  }, [appliedMachineId, load])

  const columns = [
    { key: 'changedAt', label: 'Thời điểm', render: (row: MaintenanceStatusHistoryItem) => dateTime(row.changedAt) },
    { key: 'previousStatus', label: 'Trạng thái trước', render: (row: MaintenanceStatusHistoryItem) => <StatusText value={row.previousStatus} label={machineStatuses[row.previousStatus ?? ''] ?? 'Chưa có'} /> },
    { key: 'newStatus', label: 'Trạng thái mới', render: (row: MaintenanceStatusHistoryItem) => <StatusText value={row.newStatus} label={machineStatuses[row.newStatus] ?? row.newStatus} /> },
    { key: 'sourceType', label: 'Nguồn thay đổi' },
    { key: 'sourceId', label: 'ID nguồn' },
    { key: 'note', label: 'Ghi chú' },
    { key: 'changedBy', label: 'Người thực hiện' },
  ]

  return (
    <>
      <PageTitle title="Lịch sử trạng thái máy" description="Theo dõi các lần máy chuyển sang bảo trì và trở lại trạng thái sẵn sàng." />
      <Panel title="Chọn máy trong phạm vi">
        <form className="filters hr-filters" onSubmit={(event) => { event.preventDefault(); if (Number(machineId) > 0) { setPage(0); setAppliedMachineId(Number(machineId)) } }}>
          <FilterInput label="ID máy" type="number" value={machineId} onChange={setMachineId} />
          <button type="submit">Xem lịch sử</button>
        </form>
      </Panel>
      <Panel title={appliedMachineId ? `${number(result?.totalElements)} lần thay đổi của máy #${appliedMachineId}` : 'Lịch sử trạng thái'}>
        {!appliedMachineId && <div className="module-empty"><b>Chưa chọn máy</b><p>Nhập ID máy thuộc phạm vi được cấp để xem lịch sử.</p></div>}
        <LoadingState loading={loading} error={error} />
        {appliedMachineId && <DataTable rows={result?.content ?? []} columns={columns} />}
        <Pager result={result} page={page} onPage={setPage} />
      </Panel>
    </>
  )
}

function nextRequestStatuses(status: MaintenanceRequestStatus): Option[] {
  const next: Record<MaintenanceRequestStatus, MaintenanceRequestStatus[]> = {
    OPEN: ['ACKNOWLEDGED', 'CANCELLED'],
    ACKNOWLEDGED: ['IN_PROGRESS', 'CANCELLED'],
    IN_PROGRESS: ['RESOLVED', 'CANCELLED'],
    RESOLVED: [],
    CANCELLED: [],
  }
  return next[status].map((value) => ({ value, label: optionLabel(requestStatuses, value) }))
}

function nextWorkOrderStatuses(status: MaintenanceWorkOrderStatus): Option[] {
  const next: Record<MaintenanceWorkOrderStatus, MaintenanceWorkOrderStatus[]> = {
    PLANNED: ['ASSIGNED', 'IN_PROGRESS', 'CANCELLED'],
    ASSIGNED: ['IN_PROGRESS', 'CANCELLED'],
    IN_PROGRESS: ['COMPLETED', 'CANCELLED'],
    COMPLETED: [],
    CANCELLED: [],
  }
  return next[status].map((value) => ({ value, label: optionLabel(workOrderStatuses, value) }))
}

const isTerminalWorkOrder = (status: MaintenanceWorkOrderStatus) =>
  status === 'COMPLETED' || status === 'CANCELLED'

function PageTitle({ title, description, action }: { title: string; description: string; action?: ReactNode }) {
  return <div className="page-title"><div><h2>{title}</h2><p>{description}</p></div>{action}</div>
}

function Pager<T>({ result, page, onPage }: { result?: PageResponse<T>; page: number; onPage: (page: number) => void }) {
  if (!result || result.totalPages <= 1) return null
  return (
    <div className="pagination">
      <button disabled={result.first || page <= 0} onClick={() => onPage(page - 1)}>Trang trước</button>
      <span>Trang {result.page + 1}/{result.totalPages} · {result.totalElements} bản ghi</span>
      <button disabled={result.last} onClick={() => onPage(page + 1)}>Trang sau</button>
    </div>
  )
}

function FilterInput({ label, type, value, onChange }: { label: string; type: 'number' | 'date'; value: string; onChange: (value: string) => void }) {
  return <label>{label}<input type={type} min={type === 'number' ? 1 : undefined} value={value} onChange={(event) => onChange(event.target.value)} /></label>
}

function FilterSelect({ label, value, options, onChange }: { label: string; value: string; options: Option[]; onChange: (value: string) => void }) {
  return <label>{label}<select value={value} onChange={(event) => onChange(event.target.value)}><option value="">Tất cả</option>{options.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}</select></label>
}

function StatusText({ value, label }: { value: unknown; label: string }) {
  return <span className="maintenance-status"><StatusBadge value={value} /><span>{label}</span></span>
}

function EditorModal({ title, subtitle, fields, values, onChange, busy, error, onClose, onSubmit }: {
  title: string
  subtitle: string
  fields: FieldSpec[]
  values: FormValues
  onChange: (values: FormValues) => void
  busy: boolean
  error: string
  onClose: () => void
  onSubmit: (event: FormEvent) => void
}) {
  return (
    <div className="modal-backdrop" onMouseDown={(event) => event.target === event.currentTarget && !busy && onClose()}>
      <form className="modal admin-modal maintenance-modal" onSubmit={onSubmit}>
        <div className="admin-modal-header"><div><h2>{title}</h2><p>{subtitle}</p></div><button type="button" className="modal-close" disabled={busy} onClick={onClose}>×</button></div>
        <div className="form-grid">
          {fields.map((field) => <Field key={field.key} field={field} value={values[field.key] ?? ''} onChange={(value) => onChange({ ...values, [field.key]: value })} />)}
        </div>
        {error && <p className="form-message error">{error}</p>}
        <div className="form-actions"><button type="button" disabled={busy} onClick={onClose}>Hủy</button><button type="submit" className="primary" disabled={busy}>{busy ? 'Đang xử lý…' : 'Lưu'}</button></div>
      </form>
    </div>
  )
}

function Field({ field, value, onChange }: { field: FieldSpec; value: string; onChange: (value: string) => void }) {
  return (
    <label className={`field ${field.full ? 'maintenance-field-full' : ''}`}>
      <span>{field.label}{field.required ? ' *' : ''}</span>
      {field.type === 'select' ? (
        <select required={field.required} value={value} onChange={(event) => onChange(event.target.value)}>
          <option value="">-- Chọn --</option>
          {field.options?.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
        </select>
      ) : field.type === 'textarea' ? (
        <textarea required={field.required} placeholder={field.placeholder} value={value} onChange={(event) => onChange(event.target.value)} />
      ) : (
        <input required={field.required} type={field.type ?? 'text'} min={field.min} step={field.step} placeholder={field.placeholder} value={value} onChange={(event) => onChange(event.target.value)} />
      )}
      {field.hint && <small>{field.hint}</small>}
    </label>
  )
}

function DetailModal({ title, subtitle, children, onClose, wide = false }: { title: string; subtitle: string; children: ReactNode; onClose: () => void; wide?: boolean }) {
  return (
    <div className="modal-backdrop" onMouseDown={(event) => event.target === event.currentTarget && onClose()}>
      <div className={`modal admin-modal maintenance-detail-modal ${wide ? 'wide' : ''}`}>
        <div className="admin-modal-header"><div><h2>{title}</h2><p>{subtitle}</p></div><button className="modal-close" onClick={onClose}>×</button></div>
        <div className="maintenance-detail-body">{children}</div>
      </div>
    </div>
  )
}

function DetailGrid({ values }: { values: [string, ReactNode][] }) {
  return <div className="maintenance-detail-grid">{values.map(([label, value]) => <span key={label}><small>{label}</small><b>{value}</b></span>)}</div>
}

function DetailText({ label, value }: { label: string; value?: string }) {
  if (!value) return null
  return <div className="maintenance-detail-text"><b>{label}</b><p>{value}</p></div>
}
