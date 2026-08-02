import { type FormEvent, useEffect, useState } from 'react'
import { executiveDashboardApi, type ExecutiveDashboardFilters } from '../api/executiveDashboardApi'
import { DataTable, LoadingState, Panel } from '../components/ui'
import type {
  ExecutiveDecision,
  ExecutiveFactoryPerformance,
  ExecutiveRisk,
  ExecutiveTrendPoint,
} from '../types'
import { useApi } from '../utils/useApi'

const currencyFormatter = new Intl.NumberFormat('vi-VN', {
  style: 'currency',
  currency: 'VND',
  maximumFractionDigits: 0,
})
const compactCurrencyFormatter = new Intl.NumberFormat('vi-VN', {
  notation: 'compact',
  maximumFractionDigits: 1,
})
const numberFormatter = new Intl.NumberFormat('vi-VN', { maximumFractionDigits: 2 })
const missingValue = '—'

const hasNumber = (value?: number | null): value is number =>
  typeof value === 'number' && Number.isFinite(value)

const localDate = (date: Date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const defaultFilters = (): ExecutiveDashboardFilters => {
  const today = new Date()
  return {
    fromDate: localDate(new Date(today.getFullYear(), today.getMonth(), 1)),
    toDate: localDate(today),
  }
}

const money = (value?: number | null) =>
  hasNumber(value) ? currencyFormatter.format(value) : missingValue

const compactMoney = (value?: number | null) =>
  hasNumber(value) ? `${compactCurrencyFormatter.format(value)} ₫` : missingValue

const formatNumber = (value?: number | null) =>
  hasNumber(value) ? numberFormatter.format(value) : missingValue

const percent = (value?: number | null) =>
  hasNumber(value) ? `${formatNumber(value)}%` : missingValue

const clampPercent = (value?: number | null) =>
  hasNumber(value) ? Math.min(Math.max(value, 0), 100) : 0

const dateTime = (value: string | undefined) =>
  value ? new Date(value).toLocaleString('vi-VN') : missingValue

const domainLabels: Record<string, string> = {
  FINANCE: 'Tài chính',
  PRODUCTION: 'Sản xuất',
  QUALITY: 'Chất lượng',
  MAINTENANCE: 'Bảo trì',
  RECEIVABLE: 'Công nợ phải thu',
  PAYABLE: 'Công nợ phải trả',
}

const severityLabels: Record<string, string> = {
  LOW: 'Thấp',
  MEDIUM: 'Trung bình',
  HIGH: 'Cao',
  CRITICAL: 'Khẩn cấp',
}

export default function ExecutiveDashboardPage() {
  const [draft, setDraft] = useState<ExecutiveDashboardFilters>(defaultFilters)
  const [applied, setApplied] = useState<ExecutiveDashboardFilters>(draft)
  const [filterError, setFilterError] = useState('')
  const [factoryOptions, setFactoryOptions] = useState<ExecutiveFactoryPerformance[]>([])
  const api = useApi(
    () => executiveDashboardApi.get(applied),
    `${applied.fromDate}|${applied.toDate}|${applied.factoryId ?? ''}`,
  )
  const data = api.data

  useEffect(() => {
    if (!data?.factories?.length) return
    setFactoryOptions((current) => {
      const merged = new Map(current.map((factory) => [factory.factoryId, factory]))
      data.factories.forEach((factory) => merged.set(factory.factoryId, factory))
      return [...merged.values()].sort((a, b) => a.factoryName.localeCompare(b.factoryName, 'vi'))
    })
  }, [data])

  function applyFilters(event: FormEvent) {
    event.preventDefault()
    if (!draft.fromDate || !draft.toDate) {
      setFilterError('Vui lòng chọn đầy đủ khoảng thời gian.')
      return
    }
    if (draft.fromDate > draft.toDate) {
      setFilterError('Ngày bắt đầu không được sau ngày kết thúc.')
      return
    }
    setFilterError('')
    setApplied({ ...draft })
  }

  const financial = data?.kpis?.financial
  const production = data?.kpis?.production
  const materialRisks = (data?.risks ?? []).filter((risk) =>
    ['HIGH', 'CRITICAL'].includes(risk.severity),
  ).length
  const hasData = Boolean(
    financial?.postedRecordCount ||
      production?.officialReportCount ||
      data?.risks?.length ||
      data?.decisions?.length,
  )

  return (
    <>
      <div className="page-title executive-title">
        <div>
          <span className="executive-eyebrow">BÁO CÁO ĐIỀU HÀNH</span>
          <h2>Toàn cảnh doanh nghiệp</h2>
          <p>Xu hướng tài chính, sản xuất, chất lượng và các vấn đề cần Ban giám đốc quyết định.</p>
        </div>
        <div className="executive-refresh">
          <small>Cập nhật gần nhất</small>
          <b>{dateTime(data?.generatedAt)}</b>
          <button type="button" onClick={api.reload} disabled={api.loading}>
            Làm mới
          </button>
        </div>
      </div>

      <Panel title="Phạm vi báo cáo">
        <form className="executive-filters" onSubmit={applyFilters}>
          <label>
            Từ ngày
            <input
              type="date"
              value={draft.fromDate}
              max={draft.toDate}
              onChange={(event) =>
                setDraft((current) => ({ ...current, fromDate: event.target.value }))
              }
            />
          </label>
          <label>
            Đến ngày
            <input
              type="date"
              value={draft.toDate}
              min={draft.fromDate}
              onChange={(event) =>
                setDraft((current) => ({ ...current, toDate: event.target.value }))
              }
            />
          </label>
          <label className="executive-factory-filter">
            Nhà máy
            <select
              value={draft.factoryId ?? ''}
              onChange={(event) =>
                setDraft((current) => ({
                  ...current,
                  factoryId: event.target.value ? Number(event.target.value) : undefined,
                }))
              }
            >
              <option value="">Toàn công ty</option>
              {factoryOptions.map((factory) => (
                <option key={factory.factoryId} value={factory.factoryId}>
                  {factory.factoryName}
                </option>
              ))}
            </select>
          </label>
          <button className="executive-apply" type="submit" disabled={api.loading}>
            {api.loading ? 'Đang tổng hợp…' : 'Cập nhật báo cáo'}
          </button>
          {filterError && <p className="executive-filter-error">{filterError}</p>}
        </form>
      </Panel>

      <LoadingState loading={api.loading} error={api.error} />

      {!api.loading && !api.error && data && (
        <>
          <div className="executive-scope-note">
            <span>
              Phạm vi: <b>{data.scopeName}</b>
            </span>
            <span>
              Kỳ báo cáo:{' '}
              <b>
                {data.fromDate} — {data.toDate}
              </b>
            </span>
            <span>
              Nhịp xu hướng: <b>{data.trendGranularity === 'MONTH' ? 'Theo tháng' : 'Theo ngày'}</b>
            </span>
          </div>

          <section className="executive-kpis" aria-label="Chỉ số điều hành">
            <ExecutiveKpi
              label="Doanh thu"
              value={compactMoney(financial?.revenue)}
              detail={`${formatNumber(financial?.postedRecordCount)} bút toán đã ghi nhận`}
              tone="blue"
            />
            <ExecutiveKpi
              label="Chi phí"
              value={compactMoney(financial?.expense)}
              detail={`Phải trả quá hạn ${compactMoney(financial?.overduePayable)}`}
              tone="orange"
            />
            <ExecutiveKpi
              label="Lợi nhuận"
              value={compactMoney(financial?.profit)}
              detail={`Biên lợi nhuận ${percent(financial?.profitMarginPercent)}`}
              tone={hasNumber(financial?.profit) && financial.profit < 0 ? 'red' : 'green'}
            />
            <ExecutiveKpi
              label="Sản lượng"
              value={formatNumber(production?.actualQuantity)}
              detail={`Đạt ${percent(production?.planAttainmentPercent)} kế hoạch`}
              tone="indigo"
            />
            <ExecutiveKpi
              label="Năng suất"
              value={formatNumber(production?.productivityPerHour)}
              detail="Sản phẩm / giờ vận hành"
              tone="purple"
            />
            <ExecutiveKpi
              label="Chất lượng"
              value={percent(production?.qualityPercent)}
              detail={`Tỷ lệ lỗi ${percent(production?.defectRatePercent)}`}
              tone="teal"
            />
            <ExecutiveKpi
              label="Rủi ro trọng yếu"
              value={formatNumber(materialRisks)}
              detail={`${formatNumber(data.risks.length)} cảnh báo đang được theo dõi`}
              tone={materialRisks > 0 ? 'red' : 'green'}
            />
          </section>

          {!hasData ? (
            <section className="executive-empty">
              <b>Chưa có dữ liệu điều hành trong phạm vi đã chọn</b>
              <p>
                Hãy đổi kỳ báo cáo hoặc nhà máy. Dashboard chỉ tổng hợp dữ liệu chính thức đã được
                ghi nhận.
              </p>
            </section>
          ) : (
            <>
              <div className="executive-chart-grid">
                <Panel title="Xu hướng doanh thu — chi phí — lợi nhuận">
                  <FinancialTrendChart points={data.trends} />
                </Panel>
                <Panel title="Sức khỏe vận hành">
                  <OperationalScorecard
                    plan={production?.planAttainmentPercent}
                    quality={production?.qualityPercent}
                    oee={production?.oeePercent}
                    productivity={production?.productivityPerHour}
                  />
                </Panel>
              </div>

              <Panel title="Xu hướng hiệu quả theo kỳ">
                <DataTable
                  rows={data.trends}
                  columns={[
                    { key: 'period', label: 'Kỳ' },
                    {
                      key: 'revenue',
                      label: 'Doanh thu',
                      render: (row: ExecutiveTrendPoint) => money(row.revenue),
                    },
                    {
                      key: 'expense',
                      label: 'Chi phí',
                      render: (row: ExecutiveTrendPoint) => money(row.expense),
                    },
                    {
                      key: 'profit',
                      label: 'Lợi nhuận',
                      render: (row: ExecutiveTrendPoint) => (
                        <b className={row.profit < 0 ? 'executive-negative' : ''}>
                          {money(row.profit)}
                        </b>
                      ),
                    },
                    {
                      key: 'actualQuantity',
                      label: 'Sản lượng',
                      render: (row: ExecutiveTrendPoint) => formatNumber(row.actualQuantity),
                    },
                    {
                      key: 'productivityPerHour',
                      label: 'Năng suất/giờ',
                      render: (row: ExecutiveTrendPoint) => formatNumber(row.productivityPerHour),
                    },
                    {
                      key: 'qualityPercent',
                      label: 'Chất lượng',
                      render: (row: ExecutiveTrendPoint) => percent(row.qualityPercent),
                    },
                    {
                      key: 'oeePercent',
                      label: 'OEE',
                      render: (row: ExecutiveTrendPoint) => percent(row.oeePercent),
                    },
                  ]}
                />
              </Panel>

              <div className="executive-action-grid">
                <Panel title={`Rủi ro & cảnh báo (${data.risks.length})`}>
                  <RiskList risks={data.risks} />
                </Panel>
                <Panel title={`Điểm cần quyết định (${data.decisions.length})`}>
                  <DecisionList decisions={data.decisions} />
                </Panel>
              </div>

              <Panel title="So sánh theo nhà máy">
                <FactoryPerformanceTable rows={data.factories} />
              </Panel>
            </>
          )}
        </>
      )}
    </>
  )
}

function ExecutiveKpi({
  label,
  value,
  detail,
  tone,
}: {
  label: string
  value: string
  detail: string
  tone: string
}) {
  return (
    <article className={`executive-kpi ${tone}`}>
      <span>{label}</span>
      <strong>{value}</strong>
      <small>{detail}</small>
    </article>
  )
}

function FinancialTrendChart({ points }: { points: ExecutiveTrendPoint[] }) {
  const values = points.slice(-12)
  const width = 760
  const height = 270
  const padding = { top: 22, right: 24, bottom: 48, left: 68 }
  const plotWidth = width - padding.left - padding.right
  const plotHeight = height - padding.top - padding.bottom
  const all = values.flatMap((point) => [point.revenue, point.expense, point.profit, 0])
  const minimum = Math.min(...all, 0)
  const maximum = Math.max(...all, 1)
  const span = maximum - minimum || 1
  const x = (index: number) =>
    padding.left + (values.length <= 1 ? plotWidth / 2 : (index * plotWidth) / (values.length - 1))
  const y = (value: number) => padding.top + ((maximum - value) * plotHeight) / span
  const line = (key: 'revenue' | 'expense' | 'profit') =>
    values.map((point, index) => `${x(index)},${y(point[key])}`).join(' ')
  const ticks = [0, 0.25, 0.5, 0.75, 1].map((ratio) => minimum + span * ratio)

  if (!values.length)
    return <div className="executive-panel-empty">Chưa có dữ liệu xu hướng tài chính.</div>

  return (
    <div className="executive-chart">
      <div className="executive-legend" aria-hidden="true">
        <span className="revenue">Doanh thu</span>
        <span className="expense">Chi phí</span>
        <span className="profit">Lợi nhuận</span>
      </div>
      <svg
        viewBox={`0 0 ${width} ${height}`}
        role="img"
        aria-label="Biểu đồ xu hướng doanh thu, chi phí và lợi nhuận"
      >
        {ticks.map((tick) => (
          <g key={tick}>
            <line
              className="executive-grid-line"
              x1={padding.left}
              x2={width - padding.right}
              y1={y(tick)}
              y2={y(tick)}
            />
            <text
              className="executive-axis-text"
              x={padding.left - 10}
              y={y(tick) + 4}
              textAnchor="end"
            >
              {compactMoney(tick)}
            </text>
          </g>
        ))}
        {minimum < 0 && (
          <line
            className="executive-zero-line"
            x1={padding.left}
            x2={width - padding.right}
            y1={y(0)}
            y2={y(0)}
          />
        )}
        <polyline className="executive-line revenue" points={line('revenue')} />
        <polyline className="executive-line expense" points={line('expense')} />
        <polyline className="executive-line profit" points={line('profit')} />
        {values.map((point, index) => (
          <g key={`${point.period}-${index}`}>
            {(['revenue', 'expense', 'profit'] as const).map((key) => (
              <circle
                key={key}
                className={`executive-dot ${key}`}
                cx={x(index)}
                cy={y(point[key])}
                r="3.5"
              >
                <title>{`${point.period} · ${key === 'revenue' ? 'Doanh thu' : key === 'expense' ? 'Chi phí' : 'Lợi nhuận'}: ${money(point[key])}`}</title>
              </circle>
            ))}
            <text className="executive-axis-text" x={x(index)} y={height - 18} textAnchor="middle">
              {point.period}
            </text>
          </g>
        ))}
      </svg>
    </div>
  )
}

function OperationalScorecard({
  plan,
  quality,
  oee,
  productivity,
}: {
  plan?: number
  quality?: number
  oee?: number
  productivity?: number
}) {
  const rows = [
    { label: 'Hoàn thành kế hoạch', value: plan, target: 100 },
    { label: 'Chất lượng', value: quality, target: 98 },
    { label: 'OEE', value: oee, target: 85 },
  ]
  return (
    <div className="executive-scorecard">
      {rows.map((row) => (
        <div className="executive-score" key={row.label}>
          <div>
            <span>{row.label}</span>
            <b>{percent(row.value)}</b>
          </div>
          <div className="executive-score-track">
            <span style={{ width: `${clampPercent(row.value)}%` }} />
            <i
              style={{ left: `${Math.min(row.target, 100)}%` }}
              title={`Mục tiêu ${row.target}%`}
            />
          </div>
          <small>Mục tiêu tham chiếu: {row.target}%</small>
        </div>
      ))}
      <div className="executive-productivity">
        <span>Năng suất bình quân</span>
        <strong>{formatNumber(productivity)}</strong>
        <small>sản phẩm / giờ vận hành</small>
      </div>
    </div>
  )
}

function RiskList({ risks }: { risks: ExecutiveRisk[] }) {
  if (!risks.length) {
    return (
      <div className="executive-panel-empty success">
        Không có rủi ro vượt ngưỡng trong kỳ báo cáo.
      </div>
    )
  }
  return (
    <div className="executive-risk-list">
      {risks.map((risk) => (
        <article key={risk.code} className={`executive-risk ${risk.severity.toLowerCase()}`}>
          <div>
            <span>{domainLabels[risk.domain] ?? risk.domain}</span>
            <b>{risk.title}</b>
          </div>
          <em>{severityLabels[risk.severity] ?? risk.severity}</em>
          <p>{risk.description}</p>
          <small>
            Chỉ số: {formatNumber(risk.metricValue)}
            {risk.unit ? ` ${risk.unit}` : ''}
            {risk.threshold !== undefined
              ? ` · Ngưỡng: ${formatNumber(risk.threshold)}${risk.unit ? ` ${risk.unit}` : ''}`
              : ''}
            {risk.affectedCount > 0 ? ` · Ảnh hưởng: ${risk.affectedCount}` : ''}
          </small>
        </article>
      ))}
    </div>
  )
}

function DecisionList({ decisions }: { decisions: ExecutiveDecision[] }) {
  if (!decisions.length) {
    return (
      <div className="executive-panel-empty success">
        Chưa có điểm nào cần Ban giám đốc ra quyết định.
      </div>
    )
  }
  return (
    <div className="executive-decision-list">
      {decisions.map((decision, index) => (
        <article key={`${decision.relatedRiskCode}-${index}`} className="executive-decision">
          <div className="executive-decision-number">{String(index + 1).padStart(2, '0')}</div>
          <div>
            <span>
              {domainLabels[decision.domain] ?? decision.domain} ·{' '}
              {severityLabels[decision.priority] ?? decision.priority}
            </span>
            <b>{decision.title}</b>
            <p>{decision.rationale}</p>
            <strong>Đề xuất: {decision.recommendedAction}</strong>
          </div>
        </article>
      ))}
    </div>
  )
}

function FactoryPerformanceTable({ rows }: { rows: ExecutiveFactoryPerformance[] }) {
  return (
    <DataTable
      rows={rows}
      columns={[
        { key: 'factoryName', label: 'Nhà máy' },
        {
          key: 'revenue',
          label: 'Doanh thu',
          render: (row: ExecutiveFactoryPerformance) => money(row.revenue),
        },
        {
          key: 'expense',
          label: 'Chi phí',
          render: (row: ExecutiveFactoryPerformance) => money(row.expense),
        },
        {
          key: 'profit',
          label: 'Lợi nhuận',
          render: (row: ExecutiveFactoryPerformance) => (
            <b className={row.profit < 0 ? 'executive-negative' : ''}>{money(row.profit)}</b>
          ),
        },
        {
          key: 'actualQuantity',
          label: 'Sản lượng',
          render: (row: ExecutiveFactoryPerformance) => formatNumber(row.actualQuantity),
        },
        {
          key: 'planAttainmentPercent',
          label: 'Đạt kế hoạch',
          render: (row: ExecutiveFactoryPerformance) => percent(row.planAttainmentPercent),
        },
        {
          key: 'productivityPerHour',
          label: 'Năng suất/giờ',
          render: (row: ExecutiveFactoryPerformance) => formatNumber(row.productivityPerHour),
        },
        {
          key: 'qualityPercent',
          label: 'Chất lượng',
          render: (row: ExecutiveFactoryPerformance) => percent(row.qualityPercent),
        },
        {
          key: 'oeePercent',
          label: 'OEE',
          render: (row: ExecutiveFactoryPerformance) => percent(row.oeePercent),
        },
      ]}
    />
  )
}
