import { useEffect, useState } from 'react'
import { hrApi, type HrFilters } from '../api/hrApi'
import { DataTable, LoadingState, Panel, StatusBadge } from '../components/ui'
import type { HrNotification, PageResponse, TableRow } from '../types'

export type HrView =
  | 'schedule'
  | 'attendance'
  | 'kpi'
  | 'leave'
  | 'overtime'
  | 'assignments'
  | 'notifications'

type FieldSpec = {
  key: string
  label: string
  type?: 'text' | 'number' | 'date' | 'datetime-local' | 'textarea' | 'select'
  options?: { value: string; label: string }[]
}

type ViewConfig = {
  title: string
  description: string
  addLabel?: string
  fields: FieldSpec[]
  columns: { key: string; label: string }[]
}

const configs: Record<HrView, ViewConfig> = {
  schedule: {
    title: 'Lịch làm việc bộ phận',
    description: 'Xếp lịch và điều chỉnh ca làm cho nhân viên trong phạm vi được cấp.',
    addLabel: 'Xếp lịch',
    fields: [
      { key: 'employeeId', label: 'ID nhân viên', type: 'number' },
      { key: 'shiftId', label: 'ID ca làm', type: 'number' },
      { key: 'workDate', label: 'Ngày làm', type: 'date' },
      { key: 'note', label: 'Ghi chú', type: 'textarea' },
    ],
    columns: [
      { key: 'workDate', label: 'Ngày' },
      { key: 'employeeCode', label: 'Mã NV' },
      { key: 'employeeName', label: 'Nhân viên' },
      { key: 'teamName', label: 'Tổ' },
      { key: 'shiftName', label: 'Ca' },
      { key: 'note', label: 'Ghi chú' },
      { key: 'active', label: 'Hoạt động' },
    ],
  },
  attendance: {
    title: 'Chấm công bộ phận',
    description: 'Ghi nhận hoặc điều chỉnh thời gian làm việc theo phạm vi quản lý.',
    addLabel: 'Ghi nhận chấm công',
    fields: [
      { key: 'employeeId', label: 'ID nhân viên', type: 'number' },
      { key: 'workDate', label: 'Ngày làm', type: 'date' },
      { key: 'checkIn', label: 'Vào ca', type: 'datetime-local' },
      { key: 'checkOut', label: 'Ra ca', type: 'datetime-local' },
      {
        key: 'attendanceStatus', label: 'Trạng thái', type: 'select',
        options: ['PRESENT', 'ABSENT', 'LATE', 'LEAVE_EARLY', 'ON_LEAVE'].map((value) => ({ value, label: value })),
      },
      {
        key: 'source', label: 'Nguồn', type: 'select',
        options: ['TIME_CLOCK', 'HR_IMPORT', 'MANUAL', 'ADJUSTMENT'].map((value) => ({ value, label: value })),
      },
      { key: 'note', label: 'Ghi chú', type: 'textarea' },
    ],
    columns: [
      { key: 'workDate', label: 'Ngày' },
      { key: 'employeeName', label: 'Nhân viên' },
      { key: 'teamName', label: 'Tổ' },
      { key: 'checkIn', label: 'Vào ca' },
      { key: 'checkOut', label: 'Ra ca' },
      { key: 'workingMinutes', label: 'Phút làm' },
      { key: 'overtimeMinutes', label: 'Tăng ca' },
      { key: 'attendanceStatus', label: 'Trạng thái' },
      { key: 'source', label: 'Nguồn' },
    ],
  },
  kpi: {
    title: 'KPI bộ phận',
    description: 'Đánh giá năng suất, chất lượng và chấm công của từng nhân viên.',
    addLabel: 'Nhập KPI',
    fields: [
      { key: 'employeeId', label: 'ID nhân viên', type: 'number' },
      { key: 'periodStart', label: 'Từ ngày', type: 'date' },
      { key: 'periodEnd', label: 'Đến ngày', type: 'date' },
      { key: 'productivityScore', label: 'Điểm năng suất', type: 'number' },
      { key: 'qualityScore', label: 'Điểm chất lượng', type: 'number' },
      { key: 'attendanceScore', label: 'Điểm chấm công', type: 'number' },
      { key: 'score', label: 'Tổng điểm', type: 'number' },
      { key: 'note', label: 'Nhận xét', type: 'textarea' },
    ],
    columns: [
      { key: 'employeeName', label: 'Nhân viên' },
      { key: 'teamName', label: 'Tổ' },
      { key: 'periodStart', label: 'Từ ngày' },
      { key: 'periodEnd', label: 'Đến ngày' },
      { key: 'productivityScore', label: 'Năng suất' },
      { key: 'qualityScore', label: 'Chất lượng' },
      { key: 'attendanceScore', label: 'Chấm công' },
      { key: 'score', label: 'Tổng điểm' },
    ],
  },
  leave: {
    title: 'Đơn nghỉ phép',
    description: 'Xem và phê duyệt đơn nghỉ phép trong phạm vi bộ phận.',
    fields: [],
    columns: [
      { key: 'employeeName', label: 'Nhân viên' },
      { key: 'teamName', label: 'Tổ' },
      { key: 'fromDate', label: 'Từ ngày' },
      { key: 'toDate', label: 'Đến ngày' },
      { key: 'leaveType', label: 'Loại nghỉ' },
      { key: 'reason', label: 'Lý do' },
      { key: 'status', label: 'Trạng thái' },
      { key: 'reviewComment', label: 'Nhận xét' },
    ],
  },
  overtime: {
    title: 'Đăng ký tăng ca',
    description: 'Tạo, theo dõi và phê duyệt đăng ký tăng ca theo phạm vi quản lý.',
    addLabel: 'Tạo đăng ký',
    fields: [
      { key: 'employeeId', label: 'ID nhân viên', type: 'number' },
      { key: 'workDate', label: 'Ngày tăng ca', type: 'date' },
      { key: 'requestedMinutes', label: 'Số phút', type: 'number' },
      { key: 'reason', label: 'Lý do', type: 'textarea' },
    ],
    columns: [
      { key: 'workDate', label: 'Ngày' },
      { key: 'employeeName', label: 'Nhân viên' },
      { key: 'teamName', label: 'Tổ' },
      { key: 'requestedMinutes', label: 'Số phút' },
      { key: 'reason', label: 'Lý do' },
      { key: 'status', label: 'Trạng thái' },
      { key: 'reviewComment', label: 'Nhận xét' },
    ],
  },
  assignments: {
    title: 'Điều chuyển và hỗ trợ nhân sự',
    description: 'Điều chuyển hoặc bố trí nhân viên hỗ trợ tổ khác trong một khoảng thời gian.',
    addLabel: 'Tạo phân công',
    fields: [
      { key: 'employeeId', label: 'ID nhân viên', type: 'number' },
      { key: 'targetTeamId', label: 'ID tổ nhận', type: 'number' },
      {
        key: 'assignmentType', label: 'Kiểu phân công', type: 'select',
        options: ['TRANSFERRED', 'SUPPORT'].map((value) => ({ value, label: value })),
      },
      { key: 'effectiveFrom', label: 'Từ ngày', type: 'date' },
      { key: 'effectiveTo', label: 'Đến ngày', type: 'date' },
      { key: 'reason', label: 'Lý do', type: 'textarea' },
    ],
    columns: [
      { key: 'employeeName', label: 'Nhân viên' },
      { key: 'sourceTeamName', label: 'Tổ nguồn' },
      { key: 'targetTeamName', label: 'Tổ nhận' },
      { key: 'assignmentType', label: 'Kiểu' },
      { key: 'effectiveFrom', label: 'Từ ngày' },
      { key: 'effectiveTo', label: 'Đến ngày' },
      { key: 'reason', label: 'Lý do' },
      { key: 'active', label: 'Hoạt động' },
    ],
  },
  notifications: {
    title: 'Thông báo bộ phận',
    description: 'Gửi thông báo trực tiếp tới nhân viên trong phạm vi được cấp.',
    addLabel: 'Gửi thông báo',
    fields: [
      { key: 'employeeId', label: 'ID nhân viên nhận', type: 'number' },
      { key: 'title', label: 'Tiêu đề' },
      { key: 'message', label: 'Nội dung', type: 'textarea' },
      {
        key: 'severity', label: 'Mức độ', type: 'select',
        options: ['INFO', 'SUCCESS', 'WARNING', 'CRITICAL'].map((value) => ({ value, label: value })),
      },
      { key: 'actionUrl', label: 'Đường dẫn thao tác' },
    ],
    columns: [
      { key: 'recipientName', label: 'Người nhận' },
      { key: 'title', label: 'Tiêu đề' },
      { key: 'message', label: 'Nội dung' },
      { key: 'severity', label: 'Mức độ' },
      { key: 'createdAt', label: 'Đã gửi lúc' },
    ],
  },
}

const numericKeys = new Set([
  'employeeId', 'shiftId', 'targetTeamId', 'requestedMinutes',
  'score', 'productivityScore', 'qualityScore', 'attendanceScore',
])

const badgeKeys = new Set(['status', 'attendanceStatus', 'source', 'assignmentType', 'active', 'severity'])
const emptyFilters = () => ({ fromDate: '', toDate: '', employeeId: '', teamId: '', status: '' })

export default function HrManagementPage({ view }: { view: HrView }) {
  const config = configs[view]
  const [filters, setFilters] = useState(emptyFilters)
  const [applied, setApplied] = useState(filters)
  const [result, setResult] = useState<PageResponse<TableRow>>()
  const [sentNotifications, setSentNotifications] = useState<HrNotification[]>([])
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [modalOpen, setModalOpen] = useState(false)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [form, setForm] = useState<Record<string, string>>({})
  const [busy, setBusy] = useState(false)
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    const resetFilters = emptyFilters()
    setFilters(resetFilters)
    setApplied(resetFilters)
    setPage(0)
    setResult(undefined)
    setError('')
    setMessage('')
    setModalOpen(false)
    setEditingId(null)
    setForm({})
  }, [view])

  useEffect(() => {
    if (view === 'notifications') return
    let cancelled = false
    setLoading(true)
    setError('')
    const request: HrFilters = { ...applied, page, size: 25 }

    async function load() {
      try {
        const data =
          view === 'schedule' ? await hrApi.schedules(request) :
          view === 'attendance' ? await hrApi.attendance(request) :
          view === 'kpi' ? await hrApi.kpis(request) :
          view === 'leave' ? await hrApi.leaveRequests(request) :
          view === 'overtime' ? await hrApi.overtime(request) :
          view === 'assignments' ? await hrApi.assignments(request) : undefined
        if (!cancelled) setResult(data)
      } catch (loadError) {
        if (!cancelled) setError((loadError as Error).message)
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    void load()
    return () => { cancelled = true }
  }, [view, page, applied, reloadKey])

  function openCreate() {
    const defaults: Record<string, string> = {}
    if (view === 'attendance') defaults.source = 'MANUAL'
    if (view === 'notifications') defaults.severity = 'INFO'
    setForm(defaults)
    setEditingId(null)
    setMessage('')
    setModalOpen(true)
  }

  function openEdit(row: TableRow) {
    const next: Record<string, string> = {}
    config.fields.forEach((field) => {
      const value = row[field.key]
      next[field.key] = value == null ? '' : field.type === 'datetime-local' ? String(value).slice(0, 16) : String(value)
    })
    setForm(next)
    setEditingId(Number(row.id))
    setMessage('')
    setModalOpen(true)
  }

  function payload() {
    return Object.fromEntries(
      Object.entries(form)
        .filter(([, value]) => value !== '')
        .map(([key, value]) => [key, numericKeys.has(key) ? Number(value) : value]),
    )
  }

  async function save() {
    setBusy(true)
    setMessage('')
    try {
      const data = payload()
      if (view === 'schedule') {
        const body = { ...data, active: true }
        if (editingId) await hrApi.updateSchedule(editingId, body)
        else await hrApi.createSchedule(body)
      } else if (view === 'attendance') {
        await hrApi.upsertAttendance(data)
      } else if (view === 'kpi') {
        if (editingId) await hrApi.updateKpi(editingId, data)
        else await hrApi.createKpi(data)
      } else if (view === 'overtime') {
        await hrApi.createOvertime(data)
      } else if (view === 'assignments') {
        await hrApi.createAssignment(data)
      } else if (view === 'notifications') {
        const created = await hrApi.createNotification(data)
        setSentNotifications((current) => [created, ...current])
      }
      setModalOpen(false)
      setForm({})
      setEditingId(null)
      setMessage('Đã lưu dữ liệu nhân sự thành công.')
      setReloadKey((value) => value + 1)
    } catch (saveError) {
      setMessage((saveError as Error).message)
    } finally {
      setBusy(false)
    }
  }

  async function deleteSchedule(id: number) {
    if (!confirm('Bạn có chắc muốn ngừng lịch làm việc này?')) return
    setBusy(true)
    try {
      await hrApi.deleteSchedule(id)
      setReloadKey((value) => value + 1)
    } catch (deleteError) {
      setMessage((deleteError as Error).message)
    } finally {
      setBusy(false)
    }
  }

  async function decide(kind: 'leave' | 'overtime', id: number, decision: 'APPROVED' | 'REJECTED') {
    const comment = prompt(decision === 'APPROVED' ? 'Nhận xét phê duyệt (có thể để trống):' : 'Lý do từ chối:')
    if (comment === null || (decision === 'REJECTED' && !comment.trim())) return
    setBusy(true)
    try {
      if (kind === 'leave') await hrApi.decideLeave(id, decision, comment)
      else await hrApi.decideOvertime(id, decision, comment)
      setReloadKey((value) => value + 1)
    } catch (decisionError) {
      setMessage((decisionError as Error).message)
    } finally {
      setBusy(false)
    }
  }

  async function endAssignment(id: number) {
    const endDate = prompt('Ngày kết thúc điều chuyển (YYYY-MM-DD):', new Date().toISOString().slice(0, 10))
    if (!endDate) return
    setBusy(true)
    try {
      await hrApi.endAssignment(id, endDate)
      setReloadKey((value) => value + 1)
    } catch (endError) {
      setMessage((endError as Error).message)
    } finally {
      setBusy(false)
    }
  }

  function action(row: TableRow) {
    if (view === 'schedule') return <div className="admin-actions"><button onClick={() => openEdit(row)}>Sửa</button><button className="danger-link" onClick={() => void deleteSchedule(Number(row.id))}>Xóa</button></div>
    if (view === 'attendance' || view === 'kpi') return <button onClick={() => openEdit(row)}>Sửa</button>
    if (view === 'leave' && row.status === 'PENDING') return <DecisionButtons onApprove={() => void decide('leave', Number(row.id), 'APPROVED')} onReject={() => void decide('leave', Number(row.id), 'REJECTED')} />
    if (view === 'overtime' && row.status === 'PENDING') return <DecisionButtons onApprove={() => void decide('overtime', Number(row.id), 'APPROVED')} onReject={() => void decide('overtime', Number(row.id), 'REJECTED')} />
    if (view === 'assignments' && row.active) return <button className="danger-link" onClick={() => void endAssignment(Number(row.id))}>Kết thúc</button>
    return '—'
  }

  const rows = view === 'notifications' ? sentNotifications : result?.content ?? []
  const columns = [
    ...config.columns.map((column) => ({
      ...column,
      render: (row: TableRow) => badgeKeys.has(column.key) ? <StatusBadge value={row[column.key]} /> : String(row[column.key] ?? '—'),
    })),
    ...(view === 'notifications' ? [] : [{ key: 'action', label: 'Thao tác', render: action }]),
  ]
  const statusOptions =
    view === 'attendance' ? ['PRESENT', 'ABSENT', 'LATE', 'LEAVE_EARLY', 'ON_LEAVE'] :
    view === 'leave' || view === 'overtime' ? ['PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'] : []

  return (
    <>
      <div className="page-title">
        <div><h2>{config.title}</h2><p>{config.description}</p></div>
        {config.addLabel && <button className="admin-add-button" onClick={openCreate}>+ {config.addLabel}</button>}
      </div>

      {view !== 'notifications' && (
        <Panel title="Bộ lọc theo phạm vi">
          <div className="filters hr-filters">
            <label>Từ ngày<input type="date" value={filters.fromDate} onChange={(event) => setFilters({ ...filters, fromDate: event.target.value })} /></label>
            <label>Đến ngày<input type="date" value={filters.toDate} onChange={(event) => setFilters({ ...filters, toDate: event.target.value })} /></label>
            <label>ID nhân viên<input type="number" min="1" value={filters.employeeId} onChange={(event) => setFilters({ ...filters, employeeId: event.target.value })} /></label>
            <label>ID tổ<input type="number" min="1" value={filters.teamId} onChange={(event) => setFilters({ ...filters, teamId: event.target.value })} /></label>
            {statusOptions.length > 0 && <label>Trạng thái<select value={filters.status} onChange={(event) => setFilters({ ...filters, status: event.target.value })}><option value="">Tất cả</option>{statusOptions.map((status) => <option key={status}>{status}</option>)}</select></label>}
            <button onClick={() => { setPage(0); setApplied({ ...filters }) }}>Áp dụng</button>
          </div>
        </Panel>
      )}

      {message && <p className="form-message">{message}</p>}
      <Panel title={view === 'notifications' ? 'Thông báo vừa gửi trong phiên' : `${result?.totalElements ?? 0} bản ghi`}>
        <LoadingState loading={loading} error={error} />
        <DataTable rows={rows} columns={columns} />
        {view !== 'notifications' && result && result.totalPages > 1 && (
          <div className="pagination"><button disabled={result.first} onClick={() => setPage((value) => value - 1)}>Trang trước</button><span>Trang {result.page + 1}/{result.totalPages}</span><button disabled={result.last} onClick={() => setPage((value) => value + 1)}>Trang sau</button></div>
        )}
      </Panel>
      {view === 'notifications' && <p className="hint">Backend hiện có API gửi thông báo nhưng chưa có GET `/hr/notifications`; bảng chỉ giữ các phản hồi tạo thành công trong phiên hiện tại.</p>}

      {modalOpen && (
        <div className="modal-backdrop" onMouseDown={(event) => event.target === event.currentTarget && !busy && setModalOpen(false)}>
          <div className="modal admin-modal">
            <div className="admin-modal-header"><div><h2>{editingId ? 'Cập nhật' : config.addLabel}</h2><p>{config.title}</p></div><button className="modal-close" disabled={busy} onClick={() => setModalOpen(false)}>×</button></div>
            <div className="form-grid">
              {config.fields.map((field) => <HrField key={field.key} field={field} value={form[field.key] ?? ''} onChange={(value) => setForm({ ...form, [field.key]: value })} />)}
            </div>
            {message && <p className="form-message error">{message}</p>}
            <div className="form-actions"><button disabled={busy} onClick={() => setModalOpen(false)}>Hủy</button><button className="primary" disabled={busy} onClick={() => void save()}>{busy ? 'Đang xử lý…' : 'Lưu'}</button></div>
          </div>
        </div>
      )}
    </>
  )
}

function HrField({ field, value, onChange }: { field: FieldSpec; value: string; onChange: (value: string) => void }) {
  return <label className="field"><span>{field.label}</span>{field.type === 'select' ? <select value={value} onChange={(event) => onChange(event.target.value)}><option value="">-- Chọn --</option>{field.options?.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}</select> : field.type === 'textarea' ? <textarea value={value} onChange={(event) => onChange(event.target.value)} /> : <input min={field.type === 'number' ? 0 : undefined} type={field.type ?? 'text'} value={value} onChange={(event) => onChange(event.target.value)} />}</label>
}

function DecisionButtons({ onApprove, onReject }: { onApprove: () => void; onReject: () => void }) {
  return <div className="admin-actions"><button onClick={onApprove}>Duyệt</button><button className="danger-link" onClick={onReject}>Từ chối</button></div>
}
