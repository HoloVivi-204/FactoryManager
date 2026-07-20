import { useState } from 'react'
import { employeeApi } from '../api/employeeApi'
import { DataTable, LoadingState, Panel, StatusBadge } from '../components/ui'
import { useApi } from '../utils/useApi'
import type { PageKey } from '../types'

export default function EmployeePortalPage({ view }: { view: PageKey }) {
  const portal = useApi(employeeApi.dashboard, [])
  const overtime = useApi(employeeApi.overtime, [])
  const data = portal.data
  const [leaveForm, setLeaveForm] = useState({ fromDate: '', toDate: '', leaveType: 'ANNUAL', reason: '' })
  const [overtimeForm, setOvertimeForm] = useState({ workDate: '', requestedMinutes: '60', reason: '' })
  const [message, setMessage] = useState('')
  const [busy, setBusy] = useState(false)

  async function submitLeave() {
    setBusy(true)
    setMessage('')
    try {
      await employeeApi.leave(leaveForm)
      setLeaveForm({ fromDate: '', toDate: '', leaveType: 'ANNUAL', reason: '' })
      setMessage('Đã gửi đơn nghỉ phép.')
      portal.reload()
    } catch (error) {
      setMessage((error as Error).message)
    } finally {
      setBusy(false)
    }
  }

  async function submitOvertime() {
    if (!overtimeForm.workDate || !overtimeForm.reason.trim() || Number(overtimeForm.requestedMinutes) <= 0) {
      setMessage('Vui lòng nhập ngày, số phút và lý do tăng ca hợp lệ.')
      return
    }
    setBusy(true)
    setMessage('')
    try {
      await employeeApi.createOvertime({
        workDate: overtimeForm.workDate,
        requestedMinutes: Number(overtimeForm.requestedMinutes),
        reason: overtimeForm.reason,
      })
      setOvertimeForm({ workDate: '', requestedMinutes: '60', reason: '' })
      setMessage('Đã gửi đăng ký tăng ca.')
      overtime.reload()
      portal.reload()
    } catch (error) {
      setMessage((error as Error).message)
    } finally {
      setBusy(false)
    }
  }

  if (portal.loading) return <LoadingState loading />
  if (portal.error) return <LoadingState loading={false} error={portal.error} />

  const titles: Partial<Record<PageKey, string>> = {
    overview: 'Trang cá nhân',
    schedule: 'Lịch làm việc',
    attendance: 'Chấm công và tăng ca',
    kpi: 'KPI cá nhân',
    leave: 'Nghỉ phép',
    overtime: 'Đăng ký tăng ca',
    notifications: 'Thông báo',
  }

  return (
    <>
      <div className="page-title">
        <div>
          <h2>{titles[view] ?? 'Trang cá nhân'}</h2>
          <p>{data?.employeeName} · {data?.employeeCode} · {data?.teamName}</p>
        </div>
      </div>

      {view === 'overview' && (
        <>
          <div className="kpi-grid">
            <article className="kpi blue"><span>GIỜ LÀM</span><strong>{((data?.workingMinutes ?? 0) / 60).toFixed(1)}</strong><small>giờ trong kỳ</small></article>
            <article className="kpi orange"><span>TĂNG CA</span><strong>{((data?.overtimeMinutes ?? 0) / 60).toFixed(1)}</strong><small>giờ tăng ca</small></article>
            <article className="kpi green"><span>KPI GẦN NHẤT</span><strong>{data?.kpis?.[0]?.score ?? '—'}</strong><small>điểm đánh giá</small></article>
            <article className="kpi purple"><span>THÔNG BÁO MỚI</span><strong>{data?.unreadNotifications ?? 0}</strong><small>chưa đọc</small></article>
          </div>
          <Panel title="Lịch sắp tới"><Schedule rows={data?.schedules ?? []} /></Panel>
        </>
      )}

      {view === 'schedule' && <Panel title="Lịch làm việc"><Schedule rows={data?.schedules ?? []} /></Panel>}

      {view === 'attendance' && (
        <Panel title="Chấm công">
          <DataTable rows={data?.attendance ?? []} columns={[
            { key: 'workDate', label: 'Ngày' },
            { key: 'checkIn', label: 'Vào ca' },
            { key: 'checkOut', label: 'Ra ca' },
            { key: 'workingMinutes', label: 'Phút làm' },
            { key: 'overtimeMinutes', label: 'Tăng ca' },
            { key: 'status', label: 'Trạng thái', render: (row) => <StatusBadge value={row.status} /> },
          ]} />
        </Panel>
      )}

      {view === 'kpi' && (
        <Panel title="KPI cá nhân">
          <DataTable rows={data?.kpis ?? []} columns={[
            { key: 'periodStart', label: 'Từ ngày' },
            { key: 'periodEnd', label: 'Đến ngày' },
            { key: 'productivityScore', label: 'Năng suất' },
            { key: 'qualityScore', label: 'Chất lượng' },
            { key: 'attendanceScore', label: 'Chấm công' },
            { key: 'score', label: 'Tổng điểm' },
            { key: 'note', label: 'Nhận xét' },
          ]} />
        </Panel>
      )}

      {view === 'leave' && (
        <>
          <Panel title="Tạo đơn nghỉ phép">
            <div className="form-grid">
              <label className="field"><span>Từ ngày</span><input type="date" value={leaveForm.fromDate} onChange={(event) => setLeaveForm({ ...leaveForm, fromDate: event.target.value })} /></label>
              <label className="field"><span>Đến ngày</span><input type="date" value={leaveForm.toDate} onChange={(event) => setLeaveForm({ ...leaveForm, toDate: event.target.value })} /></label>
              <label className="field"><span>Loại nghỉ</span><select value={leaveForm.leaveType} onChange={(event) => setLeaveForm({ ...leaveForm, leaveType: event.target.value })}><option value="ANNUAL">Nghỉ phép năm</option><option value="SICK">Nghỉ bệnh</option><option value="UNPAID">Nghỉ không lương</option><option value="OTHER">Khác</option></select></label>
              <label className="field"><span>Lý do</span><textarea value={leaveForm.reason} onChange={(event) => setLeaveForm({ ...leaveForm, reason: event.target.value })} /></label>
            </div>
            <div className="form-actions"><button className="primary" disabled={busy} onClick={() => void submitLeave()}>Gửi đơn</button></div>
          </Panel>
          <Panel title="Lịch sử đơn">
            <DataTable rows={data?.leaves ?? []} columns={[
              { key: 'fromDate', label: 'Từ ngày' },
              { key: 'toDate', label: 'Đến ngày' },
              { key: 'leaveType', label: 'Loại nghỉ' },
              { key: 'reason', label: 'Lý do' },
              { key: 'status', label: 'Trạng thái', render: (row) => <StatusBadge value={row.status} /> },
              { key: 'reviewComment', label: 'Phản hồi' },
            ]} />
          </Panel>
        </>
      )}

      {view === 'overtime' && (
        <>
          <Panel title="Đăng ký tăng ca">
            <div className="form-grid">
              <label className="field"><span>Ngày tăng ca</span><input type="date" value={overtimeForm.workDate} onChange={(event) => setOvertimeForm({ ...overtimeForm, workDate: event.target.value })} /></label>
              <label className="field"><span>Số phút (1-720)</span><input type="number" min="1" max="720" value={overtimeForm.requestedMinutes} onChange={(event) => setOvertimeForm({ ...overtimeForm, requestedMinutes: event.target.value })} /></label>
              <label className="field"><span>Lý do</span><textarea value={overtimeForm.reason} onChange={(event) => setOvertimeForm({ ...overtimeForm, reason: event.target.value })} /></label>
            </div>
            <div className="form-actions"><button className="primary" disabled={busy} onClick={() => void submitOvertime()}>Gửi đăng ký</button></div>
          </Panel>
          <Panel title="Lịch sử đăng ký tăng ca">
            <LoadingState loading={overtime.loading} error={overtime.error} />
            <DataTable rows={overtime.data ?? data?.overtimeRequests ?? []} columns={[
              { key: 'workDate', label: 'Ngày' },
              { key: 'requestedMinutes', label: 'Số phút' },
              { key: 'reason', label: 'Lý do' },
              { key: 'status', label: 'Trạng thái', render: (row) => <StatusBadge value={row.status} /> },
              { key: 'reviewComment', label: 'Phản hồi' },
              { key: 'createdAt', label: 'Ngày gửi' },
            ]} />
          </Panel>
        </>
      )}

      {view === 'notifications' && (
        <Panel title="Thông báo">
          <div className="notice-list">
            {(data?.notifications ?? []).map((notification: any) => (
              <button
                className={notification.read ? 'read' : ''}
                key={notification.id}
                onClick={async () => { if (!notification.read) await employeeApi.read(notification.id); portal.reload() }}
              >
                <b>{notification.title}</b>
                <span>{notification.message}</span>
                <small>{notification.createdAt}</small>
              </button>
            ))}
          </div>
        </Panel>
      )}

      {message && <p className="form-message">{message}</p>}
    </>
  )
}

function Schedule({ rows }: { rows: any[] }) {
  return <DataTable rows={rows} columns={[
    { key: 'workDate', label: 'Ngày' },
    { key: 'shiftCode', label: 'Mã ca' },
    { key: 'shiftName', label: 'Ca làm' },
    { key: 'startTime', label: 'Bắt đầu' },
    { key: 'endTime', label: 'Kết thúc' },
    { key: 'note', label: 'Ghi chú' },
  ]} />
}
