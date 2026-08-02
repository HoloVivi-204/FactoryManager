import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { dashboardApi } from '../api/dashboardApi'
import { emptyDetailBundle, shiftReportApi } from '../api/shiftReportApi'
import StagingDetailEditor from '../components/StagingDetailEditor'
import StagingExcelImporter from '../components/StagingExcelImporter'
import TeamLeaderDashboard from '../components/TeamLeaderDashboard'
import StagingReportList from '../components/StagingReportList'
import { Panel, StatusBadge } from '../components/ui'
import type { StagingDetailBundle, StagingReport, TableRow } from '../types'

type FormState = Record<string, string>

const initialForm = (): FormState => ({
  reportDate: new Date().toISOString().slice(0, 10),
  shiftId: '',
  factoryId: '',
  departmentId: '',
  productionLineId: '',
  teamId: '',
  leaderEmployeeId: '',
  machineId: '',
  plannedQuantity: '0',
  actualQuantity: '0',
  defectQuantity: '0',
  workingMinutes: '480',
  downtimeMinutes: '0',
  note: '',
})

const formFromReport = (report: StagingReport): FormState => ({
  reportDate: report.reportDate,
  shiftId: String(report.shiftId),
  factoryId: String(report.factoryId),
  departmentId: String(report.departmentId),
  productionLineId: String(report.productionLineId),
  teamId: String(report.teamId),
  leaderEmployeeId: String(report.leaderEmployeeId),
  machineId: String(report.machineId),
  plannedQuantity: String(report.plannedQuantity),
  actualQuantity: String(report.actualQuantity),
  defectQuantity: String(report.defectQuantity),
  workingMinutes: String(report.workingMinutes),
  downtimeMinutes: String(report.downtimeMinutes),
  note: report.note ?? '',
})

const numericHeaderFields = [
  'shiftId',
  'factoryId',
  'departmentId',
  'productionLineId',
  'teamId',
  'leaderEmployeeId',
  'machineId',
  'plannedQuantity',
  'actualQuantity',
  'defectQuantity',
  'workingMinutes',
  'downtimeMinutes',
]

export default function TeamLeaderReportPage() {
  const [form, setForm] = useState<FormState>(initialForm)
  const [lists, setLists] = useState<Record<string, TableRow[]>>({})
  const [drafts, setDrafts] = useState<StagingReport[]>([])
  const [scopeReports, setScopeReports] = useState<StagingReport[]>([])
  const [report, setReport] = useState<StagingReport>()
  const [details, setDetails] = useState<StagingDetailBundle>(emptyDetailBundle)
  const [editorOpen, setEditorOpen] = useState(false)
  const [tab, setTab] = useState('general')
  const [busy, setBusy] = useState(false)
  const [dirty, setDirty] = useState(false)
  const [message, setMessage] = useState('')
  const [draftFilters, setDraftFilters] = useState({
    fromDate: '',
    toDate: '',
    status: '',
    keyword: '',
  })

  const filteredDrafts = useMemo(() => {
    const term = draftFilters.keyword.trim().toLocaleLowerCase('vi')
    return drafts.filter((item) => {
      if (draftFilters.fromDate && item.reportDate < draftFilters.fromDate) return false
      if (draftFilters.toDate && item.reportDate > draftFilters.toDate) return false
      if (draftFilters.status && item.status !== draftFilters.status) return false
      if (!term) return true
      return [
        item.id,
        item.reportDate,
        item.shiftCode,
        item.shiftName,
        item.teamName,
        item.machineCode,
        item.machineName,
        item.status,
        item.note,
      ].some((value) =>
        String(value ?? '')
          .toLocaleLowerCase('vi')
          .includes(term),
      )
    })
  }, [drafts, draftFilters])

  const reloadDrafts = useCallback(async () => {
    const scopedReports = await dashboardApi.stagingMyScope()
    setScopeReports(scopedReports)
    setDrafts(
      scopedReports.filter((item) => item.status === 'DRAFT' || item.status === 'CHANGE_REQUESTED'),
    )
  }, [])

  const bootstrap = useCallback(async () => {
    setBusy(true)
    setMessage('')
    try {
      const [shifts, factories, reasons, errors, materials] = await Promise.all([
        dashboardApi.master('shifts'),
        dashboardApi.master('factories'),
        dashboardApi.master('downtime-reasons'),
        dashboardApi.master('quality-error-types'),
        dashboardApi.master('materials'),
      ])
      setLists({ shifts, factories, reasons, errors, materials })
      await reloadDrafts()
    } catch (error) {
      setMessage((error as Error).message)
    } finally {
      setBusy(false)
    }
  }, [reloadDrafts])

  useEffect(() => {
    void bootstrap()
  }, [bootstrap])

  async function loadHierarchy(current: StagingReport) {
    const [departments, lines, teams, employees, machines] = await Promise.all([
      dashboardApi.departmentsByFactory(current.factoryId),
      dashboardApi.linesByDepartment(current.departmentId),
      dashboardApi.teamsByLine(current.productionLineId),
      dashboardApi.employeesByTeam(current.teamId),
      dashboardApi.machinesByTeam(current.teamId),
    ])
    setLists((value) => ({ ...value, departments, lines, teams, employees, machines }))
  }

  async function refreshDetails(reportId = report?.id) {
    if (!reportId) return
    setDetails(await shiftReportApi.bundle(reportId))
  }

  function openNew() {
    setForm(initialForm())
    setReport(undefined)
    setDetails(emptyDetailBundle())
    setTab('general')
    setMessage('')
    setDirty(false)
    setEditorOpen(true)
  }

  async function openDraft(selected: StagingReport) {
    setBusy(true)
    setMessage('')
    try {
      const current =
        selected.status === 'CHANGE_REQUESTED'
          ? await dashboardApi.returnToDraft(selected.id)
          : await dashboardApi.getStaging(selected.id)
      await Promise.all([loadHierarchy(current), refreshDetails(current.id)])
      setReport(current)
      setForm(formFromReport(current))
      setDirty(false)
      setTab('general')
      setEditorOpen(true)
      if (selected.status === 'CHANGE_REQUESTED') {
        setMessage(`Báo cáo #${selected.id} đã được đưa về DRAFT để chỉnh sửa.`)
      }
      await reloadDrafts()
    } catch (error) {
      setMessage((error as Error).message)
    } finally {
      setBusy(false)
    }
  }

  async function deleteDraft(selected: StagingReport) {
    if (selected.status !== 'DRAFT') return
    if (
      !confirm(
        `Xóa báo cáo nháp #${selected.id}? Toàn bộ dữ liệu nhân sự, dừng máy, chất lượng và vật tư trong báo cáo cũng sẽ bị xóa.`,
      )
    )
      return
    setBusy(true)
    setMessage('')
    try {
      await dashboardApi.deleteStaging(selected.id)
      await reloadDrafts()
      setMessage(`Đã xóa báo cáo nháp #${selected.id}.`)
    } catch (error) {
      setMessage((error as Error).message)
    } finally {
      setBusy(false)
    }
  }

  async function closeEditor() {
    setEditorOpen(false)
    setReport(undefined)
    setDetails(emptyDetailBundle())
    setForm(initialForm())
    setDirty(false)
    setTab('general')
    await reloadDrafts()
  }

  const change = (key: string, value: string) => {
    setDirty(true)
    setForm((current) => ({ ...current, [key]: value }))
  }

  async function selectFactory(factoryId: string) {
    setDirty(true)
    setForm((current) => ({
      ...current,
      factoryId,
      departmentId: '',
      productionLineId: '',
      teamId: '',
      leaderEmployeeId: '',
      machineId: '',
    }))
    setLists((current) => ({
      ...current,
      departments: [],
      lines: [],
      teams: [],
      employees: [],
      machines: [],
    }))
    if (factoryId) {
      const departments = await dashboardApi.departmentsByFactory(Number(factoryId))
      setLists((current) => ({ ...current, departments }))
    }
  }

  async function selectDepartment(departmentId: string) {
    setDirty(true)
    setForm((current) => ({
      ...current,
      departmentId,
      productionLineId: '',
      teamId: '',
      leaderEmployeeId: '',
      machineId: '',
    }))
    setLists((current) => ({ ...current, lines: [], teams: [], employees: [], machines: [] }))
    if (departmentId) {
      const lines = await dashboardApi.linesByDepartment(Number(departmentId))
      setLists((current) => ({ ...current, lines }))
    }
  }

  async function selectLine(productionLineId: string) {
    setDirty(true)
    setForm((current) => ({
      ...current,
      productionLineId,
      teamId: '',
      leaderEmployeeId: '',
      machineId: '',
    }))
    setLists((current) => ({ ...current, teams: [], employees: [], machines: [] }))
    if (productionLineId) {
      const teams = await dashboardApi.teamsByLine(Number(productionLineId))
      setLists((current) => ({ ...current, teams }))
    }
  }

  async function selectTeam(teamId: string) {
    setDirty(true)
    setForm((current) => ({ ...current, teamId, leaderEmployeeId: '', machineId: '' }))
    setLists((current) => ({ ...current, employees: [], machines: [] }))
    if (teamId) {
      const [employees, machines] = await Promise.all([
        dashboardApi.employeesByTeam(Number(teamId)),
        dashboardApi.machinesByTeam(Number(teamId)),
      ])
      setLists((current) => ({ ...current, employees, machines }))
    }
  }

  function headerPayload() {
    return Object.fromEntries(
      Object.entries(form).map(([key, value]) => [
        key,
        numericHeaderFields.includes(key) ? Number(value) : value,
      ]),
    )
  }

  async function saveHeader() {
    const required = [
      'reportDate',
      'shiftId',
      'factoryId',
      'departmentId',
      'productionLineId',
      'teamId',
      'leaderEmployeeId',
      'machineId',
    ]
    if (required.some((key) => !form[key])) {
      setMessage('Vui lòng chọn đầy đủ ngày, ca, phạm vi tổ chức, tổ trưởng và máy.')
      return
    }
    setBusy(true)
    setMessage('')
    try {
      const saved = report
        ? await dashboardApi.updateStaging(report.id, headerPayload())
        : await dashboardApi.createStaging(headerPayload())
      setReport(saved)
      setForm(formFromReport(saved))
      setDirty(false)
      setMessage(`Đã lưu báo cáo nháp #${saved.id}.`)
      if (!report) setTab('downtime')
      await reloadDrafts()
    } catch (error) {
      setMessage((error as Error).message)
    } finally {
      setBusy(false)
    }
  }

  async function submit() {
    if (!report) return
    if (dirty) {
      setMessage('Thông tin chung đang có thay đổi chưa lưu. Hãy lưu trước khi gửi duyệt.')
      setTab('general')
      return
    }
    setBusy(true)
    setMessage('')
    try {
      const submitted = await dashboardApi.submit(report.id)
      setReport(submitted)
      setMessage('Đã gửi báo cáo ca cho quản lý phê duyệt.')
      await reloadDrafts()
    } catch (error) {
      setMessage((error as Error).message)
    } finally {
      setBusy(false)
    }
  }

  const tabs = [
    ['general', 'Sản lượng'],
    ['downtime', 'Máy móc / sự cố'],
    ['quality', 'Chất lượng'],
    ['material', 'Vật tư'],
    ['employee', 'Nhân sự thực tế'],
    ['review', 'Kiểm tra & gửi'],
  ]

  return (
    <>
      <div className="page-title">
        <div>
          <h2>Báo cáo thực tế trong ca</h2>
          <p>Tạo mới hoặc tiếp tục báo cáo DRAFT / CHANGE_REQUESTED trong phạm vi của bạn.</p>
        </div>
        {!editorOpen && (
          <button className="admin-add-button" onClick={openNew}>
            + Báo cáo mới
          </button>
        )}
      </div>

      {!editorOpen && (
        <>
          <TeamLeaderDashboard reports={scopeReports} />
          <StagingExcelImporter onImported={reloadDrafts} />
          <Panel title="Tìm báo cáo cần hoàn thiện">
            <div className="filters hr-filters">
              <label>
                Từ ngày
                <input
                  type="date"
                  value={draftFilters.fromDate}
                  onChange={(event) =>
                    setDraftFilters({ ...draftFilters, fromDate: event.target.value })
                  }
                />
              </label>
              <label>
                Đến ngày
                <input
                  type="date"
                  value={draftFilters.toDate}
                  onChange={(event) =>
                    setDraftFilters({ ...draftFilters, toDate: event.target.value })
                  }
                />
              </label>
              <label>
                Trạng thái
                <select
                  value={draftFilters.status}
                  onChange={(event) =>
                    setDraftFilters({ ...draftFilters, status: event.target.value })
                  }
                >
                  <option value="">Tất cả</option>
                  <option value="DRAFT">Đang nhập</option>
                  <option value="CHANGE_REQUESTED">Cần chỉnh sửa</option>
                </select>
              </label>
              <label>
                Từ khóa
                <input
                  type="search"
                  placeholder="ID, máy, ca, tổ, ghi chú…"
                  value={draftFilters.keyword}
                  onChange={(event) =>
                    setDraftFilters({ ...draftFilters, keyword: event.target.value })
                  }
                />
              </label>
              <button
                type="button"
                onClick={() =>
                  setDraftFilters({ fromDate: '', toDate: '', status: '', keyword: '' })
                }
              >
                Xóa bộ lọc
              </button>
            </div>
          </Panel>
          <Panel title={`Báo cáo cần hoàn thiện (${filteredDrafts.length}/${drafts.length})`}>
            <StagingReportList
              rows={filteredDrafts}
              action={(item) => (
                <div className="admin-actions">
                  <button disabled={busy} onClick={() => void openDraft(item)}>
                    {item.status === 'CHANGE_REQUESTED' ? 'Nhận lại & sửa' : 'Tiếp tục nhập'}
                  </button>
                  {item.status === 'DRAFT' && (
                    <button
                      className="danger-link"
                      disabled={busy}
                      onClick={() => void deleteDraft(item)}
                    >
                      Xóa nháp
                    </button>
                  )}
                </div>
              )}
            />
          </Panel>
        </>
      )}

      {editorOpen && (
        <>
          <div className="editor-toolbar">
            <div>
              <b>{report ? `Báo cáo #${report.id}` : 'Báo cáo mới'}</b>
              {report && <StatusBadge value={report.status} />}
            </div>
            <button disabled={busy} onClick={() => void closeEditor()}>
              Đóng trình nhập
            </button>
          </div>
          {report?.reviewComment && (
            <p className="review-comment">
              <b>Yêu cầu chỉnh sửa:</b> {report.reviewComment}
            </p>
          )}
          <div className="step-tabs">
            {tabs.map(([key, label]) => (
              <button
                key={key}
                className={tab === key ? 'active' : ''}
                disabled={key !== 'general' && !report}
                onClick={() => setTab(key)}
              >
                {label}
              </button>
            ))}
          </div>

          {tab === 'general' && (
            <Panel title="Thông tin ca và sản lượng">
              <div className="form-grid">
                <Field label="Ngày">
                  <input
                    type="date"
                    value={form.reportDate}
                    onChange={(e) => change('reportDate', e.target.value)}
                  />
                </Field>
                <Select
                  label="Ca"
                  value={form.shiftId}
                  rows={lists.shifts ?? []}
                  onChange={(v) => change('shiftId', v)}
                />
                <Select
                  label="Nhà máy"
                  value={form.factoryId}
                  rows={lists.factories ?? []}
                  onChange={(v) => void selectFactory(v)}
                />
                <Select
                  label="Phòng ban"
                  value={form.departmentId}
                  rows={lists.departments ?? []}
                  onChange={(v) => void selectDepartment(v)}
                />
                <Select
                  label="Dây chuyền"
                  value={form.productionLineId}
                  rows={lists.lines ?? []}
                  onChange={(v) => void selectLine(v)}
                />
                <Select
                  label="Tổ"
                  value={form.teamId}
                  rows={lists.teams ?? []}
                  onChange={(v) => void selectTeam(v)}
                />
                <Select
                  label="Tổ trưởng"
                  value={form.leaderEmployeeId}
                  rows={lists.employees ?? []}
                  name="fullName"
                  onChange={(v) => change('leaderEmployeeId', v)}
                />
                <Select
                  label="Máy"
                  value={form.machineId}
                  rows={lists.machines ?? []}
                  onChange={(v) => change('machineId', v)}
                />
                {[
                  ['plannedQuantity', 'Kế hoạch'],
                  ['actualQuantity', 'Thực tế'],
                  ['defectQuantity', 'Hàng lỗi'],
                  ['workingMinutes', 'Phút làm'],
                  ['downtimeMinutes', 'Phút dừng'],
                ].map(([key, label]) => (
                  <Field key={key} label={label}>
                    <input
                      type="number"
                      min="0"
                      value={form[key]}
                      onChange={(e) => change(key, e.target.value)}
                    />
                  </Field>
                ))}
                <Field label="Ghi chú">
                  <textarea value={form.note} onChange={(e) => change('note', e.target.value)} />
                </Field>
              </div>
              <div className="form-actions">
                <button
                  className="primary"
                  disabled={busy || report?.status === 'SUBMITTED'}
                  onClick={() => void saveHeader()}
                >
                  {busy ? 'Đang xử lý…' : report ? 'Lưu thông tin chung' : 'Lưu nháp và tiếp tục'}
                </button>
              </div>
            </Panel>
          )}

          {report && tab === 'downtime' && (
            <StagingDetailEditor
              path="machine-downtime-staging"
              title="Ghi nhận máy dừng"
              reportId={report.id}
              rows={details['machine-downtime-staging']}
              editable={report.status === 'DRAFT'}
              selects={[
                { key: 'machineId', label: 'Máy', rows: lists.machines ?? [] },
                { key: 'downtimeReasonId', label: 'Nguyên nhân', rows: lists.reasons ?? [] },
              ]}
              fields={[
                { key: 'startTime', label: 'Bắt đầu', type: 'datetime-local' },
                { key: 'endTime', label: 'Kết thúc', type: 'datetime-local' },
                { key: 'description', label: 'Mô tả', type: 'textarea' },
              ]}
              columns={[
                { key: 'machineCode', label: 'Máy' },
                { key: 'downtimeReasonName', label: 'Nguyên nhân' },
                { key: 'startTime', label: 'Bắt đầu' },
                { key: 'endTime', label: 'Kết thúc' },
                { key: 'durationMinutes', label: 'Số phút' },
              ]}
              onChanged={() => refreshDetails(report.id)}
            />
          )}

          {report && tab === 'quality' && (
            <StagingDetailEditor
              path="quality-report-staging"
              title="Ghi nhận lỗi chất lượng"
              reportId={report.id}
              rows={details['quality-report-staging']}
              editable={report.status === 'DRAFT'}
              selects={[{ key: 'qualityErrorTypeId', label: 'Loại lỗi', rows: lists.errors ?? [] }]}
              fields={[
                { key: 'quantity', label: 'Số lượng lỗi', type: 'number' },
                { key: 'description', label: 'Mô tả', type: 'textarea' },
              ]}
              columns={[
                { key: 'qualityErrorTypeName', label: 'Loại lỗi' },
                { key: 'quantity', label: 'Số lượng' },
                { key: 'description', label: 'Mô tả' },
              ]}
              onChanged={() => refreshDetails(report.id)}
            />
          )}

          {report && tab === 'material' && (
            <StagingDetailEditor
              path="material-issue-staging"
              title="Ghi nhận sự cố vật tư"
              reportId={report.id}
              rows={details['material-issue-staging']}
              editable={report.status === 'DRAFT'}
              selects={[
                { key: 'materialId', label: 'Vật tư', rows: lists.materials ?? [] },
                {
                  key: 'issueType',
                  label: 'Loại sự cố',
                  rows: [
                    { id: 'SHORTAGE', name: 'Thiếu vật tư' },
                    { id: 'LATE_DELIVERY', name: 'Giao chậm' },
                    { id: 'WRONG_SPECIFICATION', name: 'Sai quy cách' },
                    { id: 'DAMAGED', name: 'Hư hỏng' },
                    { id: 'QUALITY_FAILED', name: 'Không đạt chất lượng' },
                    { id: 'OTHER', name: 'Khác' },
                  ],
                },
              ]}
              fields={[
                { key: 'quantity', label: 'Số lượng', type: 'number' },
                { key: 'unit', label: 'Đơn vị' },
                { key: 'description', label: 'Mô tả', type: 'textarea' },
              ]}
              columns={[
                { key: 'materialName', label: 'Vật tư' },
                { key: 'issueType', label: 'Loại sự cố' },
                { key: 'quantity', label: 'Số lượng' },
                { key: 'unit', label: 'Đơn vị' },
              ]}
              onChanged={() => refreshDetails(report.id)}
            />
          )}

          {report && tab === 'employee' && (
            <StagingDetailEditor
              path="employee-actual-staging"
              title="Xác nhận nhân sự thực tế"
              reportId={report.id}
              rows={details['employee-actual-staging']}
              editable={report.status === 'DRAFT'}
              selects={[
                { key: 'employeeId', label: 'Nhân viên', rows: lists.employees ?? [] },
                {
                  key: 'attendanceStatus',
                  label: 'Có mặt',
                  rows: [
                    { id: 'PRESENT', name: 'Có mặt' },
                    { id: 'ABSENT', name: 'Vắng' },
                    { id: 'LATE', name: 'Đi muộn' },
                    { id: 'LEAVE_EARLY', name: 'Về sớm' },
                    { id: 'ON_LEAVE', name: 'Nghỉ phép' },
                  ],
                },
                {
                  key: 'assignmentType',
                  label: 'Phân công',
                  rows: [
                    { id: 'NORMAL', name: 'Đúng tổ' },
                    { id: 'TRANSFERRED', name: 'Điều chuyển' },
                    { id: 'SUPPORT', name: 'Hỗ trợ' },
                    { id: 'OVERTIME', name: 'Tăng ca' },
                  ],
                },
              ]}
              fields={[
                { key: 'workingMinutes', label: 'Phút làm', type: 'number' },
                { key: 'overtimeMinutes', label: 'Phút tăng ca', type: 'number' },
                { key: 'description', label: 'Ghi chú', type: 'textarea' },
              ]}
              columns={[
                { key: 'employeeName', label: 'Nhân viên' },
                { key: 'attendanceStatus', label: 'Có mặt' },
                { key: 'workingMinutes', label: 'Phút làm' },
                { key: 'overtimeMinutes', label: 'Tăng ca' },
                { key: 'assignmentType', label: 'Phân công' },
              ]}
              onChanged={() => refreshDetails(report.id)}
            />
          )}

          {tab === 'review' && (
            <Panel title="Kiểm tra và gửi báo cáo">
              <div className="review-summary">
                <p>
                  Báo cáo: <b>#{report?.id ?? 'Chưa lưu'}</b>
                </p>
                <p>
                  Thực tế: <b>{form.actualQuantity}</b> · Lỗi: <b>{form.defectQuantity}</b> ·
                  Downtime: <b>{form.downtimeMinutes} phút</b>
                </p>
                <p>
                  Downtime: {details['machine-downtime-staging'].length} · Loại lỗi:{' '}
                  {details['quality-report-staging'].length} · Vật tư:{' '}
                  {details['material-issue-staging'].length} · Nhân sự:{' '}
                  {details['employee-actual-staging'].length}
                </p>
                <p className="hint-inline">
                  Hãy lưu lại thông tin chung sau khi điều chỉnh tổng lỗi hoặc tổng phút dừng.
                </p>
              </div>
              <div className="form-actions">
                <button
                  className="primary"
                  disabled={busy || dirty || !report || report.status !== 'DRAFT'}
                  onClick={() => void submit()}
                >
                  Xác nhận và gửi duyệt
                </button>
              </div>
            </Panel>
          )}
        </>
      )}

      {message && <p className="form-message">{message}</p>}
    </>
  )
}

function Field({ label, children }: { label: string; children: ReactNode }) {
  return (
    <label className="field">
      <span>{label}</span>
      {children}
    </label>
  )
}

function Select({
  label,
  value,
  rows,
  name = 'name',
  onChange,
}: {
  label: string
  value: string
  rows: TableRow[]
  name?: string
  onChange: (value: string) => void
}) {
  return (
    <Field label={label}>
      <select value={value} onChange={(event) => onChange(event.target.value)}>
        <option value="">-- Chọn --</option>
        {rows.map((row) => (
          <option key={String(row.id)} value={String(row.id ?? '')}>
            {row.code ? `${String(row.code)} - ` : ''}
            {String(row[name] ?? row.id ?? '')}
          </option>
        ))}
      </select>
    </Field>
  )
}
