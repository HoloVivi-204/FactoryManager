import { useEffect, useState, type ReactNode } from 'react'
import { dashboardApi } from '../api/dashboardApi'
import { Panel, StatusBadge } from '../components/ui'
import type { StagingReport, TableRow } from '../types'

const initial = {
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
}

type ReportForm = typeof initial
type SelectField = readonly [keyof ReportForm, string, string, string]
type NumberField = readonly [keyof ReportForm, string]

const selectFields: SelectField[] = [
  ['shiftId', 'Ca làm', 'code', 'name'],
  ['factoryId', 'Nhà máy', 'code', 'name'],
  ['departmentId', 'Phòng ban', 'code', 'name'],
  ['productionLineId', 'Dây chuyền', 'code', 'name'],
  ['teamId', 'Tổ', 'code', 'name'],
  ['leaderEmployeeId', 'Tổ trưởng', 'code', 'fullName'],
  ['machineId', 'Máy', 'code', 'name'],
]

const numberFields: NumberField[] = [
  ['plannedQuantity', 'Kế hoạch'],
  ['actualQuantity', 'Thực tế'],
  ['defectQuantity', 'Sản phẩm lỗi'],
  ['workingMinutes', 'Phút làm việc'],
  ['downtimeMinutes', 'Phút dừng máy'],
]

const numericKeys = new Set<keyof ReportForm>([
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
])

const optionText = (row: TableRow, codeKey: string, nameKey: string) =>
  `${String(row[codeKey] ?? '')} - ${String(row[nameKey] ?? '')}`

export default function ReportEntryPage() {
  const [form, setForm] = useState<ReportForm>(initial)
  const [lists, setLists] = useState<Record<string, TableRow[]>>({})
  const [saved, setSaved] = useState<StagingReport>()
  const [message, setMessage] = useState('')
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    Promise.all(
      selectFields.map(async ([, , ,], index) => {
        const paths = ['shifts', 'factories', 'departments', 'production-lines', 'teams', 'employees', 'machines']
        const [key] = selectFields[index]
        return [key, await dashboardApi.master(paths[index])] as const
      }),
    )
      .then((entries) => setLists(Object.fromEntries(entries)))
      .catch((error) => setMessage((error as Error).message))
  }, [])

  const change = (key: keyof ReportForm, value: string) => setForm((current) => ({ ...current, [key]: value }))

  async function save() {
    setBusy(true)
    try {
      const data = Object.fromEntries(
        Object.entries(form).map(([key, value]) =>
          numericKeys.has(key as keyof ReportForm) ? [key, Number(value)] : [key, value],
        ),
      )
      setSaved(await dashboardApi.createStaging(data))
      setMessage('Đã lưu bản nháp. Hãy bổ sung các dữ liệu chi tiết trước khi gửi duyệt.')
    } catch (error) {
      setMessage((error as Error).message)
    } finally {
      setBusy(false)
    }
  }

  async function submit() {
    if (!saved) return
    setBusy(true)
    try {
      setSaved(await dashboardApi.submit(saved.id))
      setMessage('Đã gửi báo cáo cho quản lý.')
    } catch (error) {
      setMessage((error as Error).message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <>
      <div className="page-title">
        <div>
          <h2>Nhập báo cáo ca</h2>
          <p>Nhập sản lượng, lưu nháp và gửi quản lý phê duyệt</p>
        </div>
        {saved && <StatusBadge value={saved.status} />}
      </div>

      <Panel title="Thông tin chung">
        <div className="form-grid">
          <Field label="Ngày">
            <input type="date" value={form.reportDate} onChange={(event) => change('reportDate', event.target.value)} />
          </Field>

          {selectFields.map(([key, label, codeKey, nameKey]) => (
            <Field label={label} key={key}>
              <select value={form[key]} onChange={(event) => change(key, event.target.value)}>
                <option value="">-- Chọn --</option>
                {(lists[key] ?? []).map((row) => (
                  <option key={String(row.id)} value={String(row.id ?? '')}>
                    {optionText(row, codeKey, nameKey)}
                  </option>
                ))}
              </select>
            </Field>
          ))}

          {numberFields.map(([key, label]) => (
            <Field label={label} key={key}>
              <input
                type="number"
                min="0"
                value={form[key]}
                onChange={(event) => change(key, event.target.value)}
              />
            </Field>
          ))}

          <Field label="Ghi chú">
            <textarea value={form.note} onChange={(event) => change('note', event.target.value)} />
          </Field>
        </div>

        <div className="form-actions">
          <button className="primary" disabled={busy || !!saved} onClick={() => void save()}>Lưu bản nháp</button>
          <button disabled={busy || !saved || saved.status !== 'DRAFT'} onClick={() => void submit()}>Gửi duyệt</button>
        </div>
        {message && <p className="form-message">{message}</p>}
      </Panel>

      {saved && (
        <Panel title={`Dữ liệu chi tiết báo cáo #${saved.id}`}>
          <div className="detail-links">
            <span>Dừng máy</span>
            <span>Lỗi chất lượng</span>
            <span>Sự cố vật tư</span>
            <span>Nhân sự thực tế</span>
          </div>
          <p className="hint">Tổng lỗi chất lượng chi tiết phải bằng số sản phẩm lỗi trước khi gửi duyệt.</p>
        </Panel>
      )}
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
