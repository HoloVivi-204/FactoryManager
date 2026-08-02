import type { ReactNode } from 'react'

export type TableColumn<T extends object = Record<string, unknown>> = {
  key: string
  label: string
  render?: (row: T) => ReactNode
}

const getCellValue = (row: object, key: string) => (row as Record<string, unknown>)[key]

const getRowKey = (row: object, index: number) => {
  const id = (row as { id?: unknown }).id
  return typeof id === 'number' || typeof id === 'string' ? id : index
}

export function Panel({
  title,
  children,
  action,
}: {
  title: string
  children: ReactNode
  action?: ReactNode
}) {
  return (
    <section className="panel">
      <header>
        <h2>{title}</h2>
        {action}
      </header>
      {children}
    </section>
  )
}

export function KpiCard({
  label,
  value,
  hint,
  tone = 'blue',
}: {
  label: string
  value: string
  hint: string
  tone?: string
}) {
  return (
    <article className={`kpi ${tone}`}>
      <span>{label}</span>
      <strong>{value}</strong>
      <small>{hint}</small>
    </article>
  )
}

export function StatusBadge({ value }: { value: unknown }) {
  const text = String(value ?? '—')
  return <span className={`badge ${text.toLowerCase()}`}>{text}</span>
}

export function LoadingState({ loading, error }: { loading: boolean; error?: string }) {
  if (loading) return <div className="state">Đang tải dữ liệu...</div>
  if (error) return <div className="state error">{error}</div>
  return null
}

export function DataTable<T extends object>({
  columns,
  rows,
}: {
  columns: TableColumn<T>[]
  rows: T[]
}) {
  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            {columns.map((column) => (
              <th key={column.key}>{column.label}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row, index) => (
            <tr key={getRowKey(row, index)}>
              {columns.map((column) => (
                <td key={column.key}>
                  {column.render
                    ? column.render(row)
                    : String(getCellValue(row, column.key) ?? '—')}
                </td>
              ))}
            </tr>
          ))}
          {!rows.length && (
            <tr>
              <td colSpan={columns.length} className="empty">
                Chưa có dữ liệu
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
