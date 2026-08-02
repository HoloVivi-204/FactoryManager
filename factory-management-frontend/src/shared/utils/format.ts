const missingValue = '—'

export const nf = new Intl.NumberFormat('vi-VN')

const hasNumber = (value?: number | null): value is number =>
  typeof value === 'number' && Number.isFinite(value)

export const number = (value?: number | null) =>
  hasNumber(value) ? nf.format(value) : missingValue

export const percent = (value?: number | null) =>
  hasNumber(value) ? `${value.toFixed(2)}%` : missingValue

export const text = (value: unknown) =>
  value == null || value === '' ? missingValue : String(value)
