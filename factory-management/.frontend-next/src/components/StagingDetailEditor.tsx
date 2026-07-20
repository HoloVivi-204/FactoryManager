import { useState, type ReactNode } from 'react'
import { shiftReportApi } from '../api/shiftReportApi'
import type { StagingDetailPath, TableRow } from '../types'
import { DataTable, Panel } from './ui'

type Option = Record<string, any>
export type DetailSelect = { key: string; label: string; rows: Option[] }
export type DetailField = {
  key: string
  label: string
  type?: 'text' | 'number' | 'datetime-local' | 'textarea'
}
export type DetailColumn = { key: string; label: string }

const numericKeys = new Set([
  'machineId',
  'downtimeReasonId',
  'qualityErrorTypeId',
  'materialId',
  'employeeId',
  'quantity',
  'workingMinutes',
  'overtimeMinutes',
])

const inputValue = (value: unknown, type?: DetailField['type']) => {
  if (value == null) return ''
  if (type === 'datetime-local') return String(value).slice(0, 16)
  return String(value)
}

export default function StagingDetailEditor({
  path,
  title,
  reportId,
  rows,
  selects,
  fields,
  columns,
  editable,
  onChanged,
}: {
  path: StagingDetailPath
  title: string
  reportId: number
  rows: TableRow[]
  selects: DetailSelect[]
  fields: DetailField[]
  columns: DetailColumn[]
  editable: boolean
  onChanged: () => Promise<void>
}) {
  const [form, setForm] = useState<Record<string, string>>({})
  const [editingId, setEditingId] = useState<number | null>(null)
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState('')

  const set = (key: string, value: string) => setForm((current) => ({ ...current, [key]: value }))

  const payload = () =>
    Object.fromEntries(
      Object.entries(form)
        .filter(([, value]) => value !== '')
        .map(([key, value]) => [key, numericKeys.has(key) ? Number(value) : value]),
    )

  const reset = () => {
    setForm({})
    setEditingId(null)
    setMessage('')
  }

  const edit = (row: TableRow) => {
    const next: Record<string, string> = {}
    selects.forEach(({ key }) => (next[key] = inputValue(row[key])))
    fields.forEach(({ key, type }) => (next[key] = inputValue(row[key], type)))
    setForm(next)
    setEditingId(Number(row.id))
    setMessage('')
  }

  const save = async () => {
    if (!editable) return
    setBusy(true)
    setMessage('')
    try {
      const data = { ...payload(), productionReportStagingId: reportId, active: true }
      if (editingId) await shiftReportApi.updateDetail(path, editingId, data)
      else await shiftReportApi.createDetail(path, data)
      reset()
      await onChanged()
    } catch (error) {
      setMessage((error as Error).message)
    } finally {
      setBusy(false)
    }
  }

  const remove = async (id: number) => {
    if (!editable || !confirm('Bạn có chắc muốn xóa bản ghi chi tiết này?')) return
    setBusy(true)
    setMessage('')
    try {
      await shiftReportApi.deleteDetail(path, id)
      if (editingId === id) reset()
      await onChanged()
    } catch (error) {
      setMessage((error as Error).message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <>
      <Panel title={editingId ? `Sửa ${title.toLowerCase()} #${editingId}` : title}>
        <div className="form-grid">
          {selects.map((item) => (
            <Field key={item.key} label={item.label}>
              <select
                disabled={!editable || busy}
                value={form[item.key] ?? ''}
                onChange={(event) => set(item.key, event.target.value)}
              >
                <option value="">-- Chọn --</option>
                {item.rows.map((option) => (
                  <option key={option.id} value={option.id}>
                    {option.code ? `${option.code} - ` : ''}
                    {option.name ?? option.fullName ?? option.id}
                  </option>
                ))}
              </select>
            </Field>
          ))}
          {fields.map((item) => (
            <Field key={item.key} label={item.label}>
              {item.type === 'textarea' ? (
                <textarea
                  disabled={!editable || busy}
                  value={form[item.key] ?? ''}
                  onChange={(event) => set(item.key, event.target.value)}
                />
              ) : (
                <input
                  disabled={!editable || busy}
                  min={item.type === 'number' ? 0 : undefined}
                  type={item.type ?? 'text'}
                  value={form[item.key] ?? ''}
                  onChange={(event) => set(item.key, event.target.value)}
                />
              )}
            </Field>
          ))}
        </div>
        {message && <p className="form-message error">{message}</p>}
        {editable && (
          <div className="form-actions">
            {editingId && <button onClick={reset}>Hủy sửa</button>}
            <button className="primary" disabled={busy} onClick={() => void save()}>
              {busy ? 'Đang xử lý…' : editingId ? 'Lưu thay đổi' : 'Ghi nhận'}
            </button>
          </div>
        )}
      </Panel>
      <Panel title={`Đã ghi nhận (${rows.length})`}>
        <DataTable
          rows={rows}
          columns={[
            ...columns,
            ...(editable
              ? [
                  {
                    key: 'action',
                    label: 'Thao tác',
                    render: (row: TableRow) => (
                      <div className="admin-actions">
                        <button disabled={busy} onClick={() => edit(row)}>Sửa</button>
                        <button
                          className="danger-link"
                          disabled={busy}
                          onClick={() => void remove(Number(row.id))}
                        >
                          Xóa
                        </button>
                      </div>
                    ),
                  },
                ]
              : []),
          ]}
        />
      </Panel>
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
