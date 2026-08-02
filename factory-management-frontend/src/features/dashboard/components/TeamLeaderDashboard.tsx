import { useEffect, useMemo, useState } from 'react'
import { shiftReportApi } from '../../production-reports/api/shiftReportApi'
import type { StagingReport, TableRow } from '../../../shared/types'
import { Panel, StatusBadge } from '../../../shared/ui/ui'

const nf = new Intl.NumberFormat('vi-VN')
const emptyDetails = () => ({
  downtime: [] as TableRow[],
  quality: [] as TableRow[],
  materials: [] as TableRow[],
  employees: [] as TableRow[],
})

function localIsoDate(date = new Date()) {
  const offset = date.getTimezoneOffset() * 60_000
  return new Date(date.getTime() - offset).toISOString().slice(0, 10)
}

function startDate(days: number) {
  const date = new Date()
  date.setDate(date.getDate() - days + 1)
  return localIsoDate(date)
}

const sum = (rows: StagingReport[], key: keyof StagingReport) =>
  rows.reduce((total, row) => total + Number(row[key] ?? 0), 0)
const percentage = (value: number) => `${value.toFixed(1)}%`
const dateLabel = (value: string) => value.split('-').reverse().join('/')

export default function TeamLeaderDashboard({ reports }: { reports: StagingReport[] }) {
  const [days, setDays] = useState(30)
  const [fromDate, setFromDate] = useState(() => startDate(30))
  const [toDate, setToDate] = useState(() => localIsoDate())
  const [keyword, setKeyword] = useState('')
  const [details, setDetails] = useState(emptyDetails)
  const [detailsLoading, setDetailsLoading] = useState(false)
  const [detailsError, setDetailsError] = useState('')
  const today = localIsoDate()

  const filtered = useMemo(() => {
    const term = keyword.trim().toLocaleLowerCase('vi')
    return reports.filter((row) => {
      if (fromDate && row.reportDate < fromDate) return false
      if (toDate && row.reportDate > toDate) return false
      if (!term) return true
      return [
        row.id,
        row.reportDate,
        row.shiftCode,
        row.shiftName,
        row.teamName,
        row.machineCode,
        row.machineName,
        row.status,
        row.note,
      ].some((value) =>
        String(value ?? '')
          .toLocaleLowerCase('vi')
          .includes(term),
      )
    })
  }, [reports, fromDate, toDate, keyword])

  function selectPeriod(value: number) {
    setDays(value)
    setFromDate(startDate(value))
    setToDate(localIsoDate())
  }

  function clearFilters() {
    setDays(30)
    setFromDate(startDate(30))
    setToDate(localIsoDate())
    setKeyword('')
  }

  useEffect(() => {
    let alive = true
    async function loadDetails() {
      setDetailsLoading(true)
      setDetailsError('')
      try {
        const bundles = await Promise.all(
          filtered.map((report) => shiftReportApi.bundle(report.id)),
        )
        if (!alive) return
        setDetails({
          downtime: bundles.flatMap((bundle) => bundle['machine-downtime-staging']),
          quality: bundles.flatMap((bundle) => bundle['quality-report-staging']),
          materials: bundles.flatMap((bundle) => bundle['material-issue-staging']),
          employees: bundles.flatMap((bundle) => bundle['employee-actual-staging']),
        })
      } catch (loadError) {
        if (alive) {
          setDetails(emptyDetails())
          setDetailsError((loadError as Error).message)
        }
      } finally {
        if (alive) setDetailsLoading(false)
      }
    }
    void loadDetails()
    return () => {
      alive = false
    }
  }, [filtered])

  const metrics = useMemo(() => {
    const planned = sum(filtered, 'plannedQuantity')
    const actual = sum(filtered, 'actualQuantity')
    const defect = sum(filtered, 'defectQuantity')
    const good = filtered.reduce(
      (total, row) => total + Number(row.goodQuantity ?? row.actualQuantity - row.defectQuantity),
      0,
    )
    const downtime = sum(filtered, 'downtimeMinutes')
    const working = sum(filtered, 'workingMinutes')
    return {
      planned,
      actual,
      good,
      defect,
      downtime,
      working,
      gap: actual - planned,
      attainment: planned > 0 ? (actual * 100) / planned : 0,
      defectRate: actual > 0 ? (defect * 100) / actual : 0,
      downtimeRate: working > 0 ? (downtime * 100) / working : 0,
    }
  }, [filtered])

  const workflow = useMemo(
    () => ({
      draft: filtered.filter((row) => row.status === 'DRAFT').length,
      change: filtered.filter((row) => row.status === 'CHANGE_REQUESTED').length,
      submitted: filtered.filter((row) => row.status === 'SUBMITTED').length,
      completed: filtered.filter((row) => row.status === 'APPROVED' || row.status === 'LOCKED')
        .length,
    }),
    [filtered],
  )

  const people = useMemo(() => {
    const workingRows = details.employees.filter(
      (row) => row.attendanceStatus !== 'ABSENT' && row.attendanceStatus !== 'ON_LEAVE',
    )
    return {
      turns: workingRows.length,
      unique: new Set(workingRows.map((row) => row.employeeId)).size,
      absent: details.employees.filter((row) => row.attendanceStatus === 'ABSENT').length,
      late: details.employees.filter((row) => row.attendanceStatus === 'LATE').length,
      overtime: details.employees.reduce(
        (total, row) => total + Number(row.overtimeMinutes ?? 0),
        0,
      ),
      working: details.employees.reduce((total, row) => total + Number(row.workingMinutes ?? 0), 0),
    }
  }, [details.employees])

  const trend = useMemo(() => {
    const values = new Map<
      string,
      { planned: number; actual: number; good: number; defect: number; downtime: number }
    >()
    filtered.forEach((row) => {
      const current = values.get(row.reportDate) ?? {
        planned: 0,
        actual: 0,
        good: 0,
        defect: 0,
        downtime: 0,
      }
      current.planned += row.plannedQuantity
      current.actual += row.actualQuantity
      current.good += row.goodQuantity ?? row.actualQuantity - row.defectQuantity
      current.defect += row.defectQuantity
      current.downtime += row.downtimeMinutes
      values.set(row.reportDate, current)
    })
    return [...values.entries()].sort(([a], [b]) => a.localeCompare(b))
  }, [filtered])

  const peopleByReport = useMemo(() => {
    const counts = new Map<number, number>()
    details.employees.forEach((row) => {
      if (row.attendanceStatus === 'ABSENT' || row.attendanceStatus === 'ON_LEAVE') return
      const reportId = Number(row.productionReportStagingId)
      counts.set(reportId, (counts.get(reportId) ?? 0) + 1)
    })
    return counts
  }, [details.employees])

  const downtimeReasons = useMemo(
    () =>
      group(
        details.downtime,
        (row) => String(row.downtimeReasonName ?? row.downtimeReasonCode ?? 'Chưa xác định'),
        (row) => Number(row.durationMinutes ?? 0),
        ' phút',
      ),
    [details.downtime],
  )
  const qualityTypes = useMemo(
    () =>
      group(
        details.quality,
        (row) => String(row.qualityErrorTypeName ?? row.qualityErrorTypeCode ?? 'Chưa xác định'),
        (row) => Number(row.quantity ?? 0),
        ' lỗi',
      ),
    [details.quality],
  )
  const materialTypes = useMemo(
    () =>
      group(
        details.materials,
        (row) => issueLabel(String(row.issueType ?? 'OTHER')),
        () => 1,
        ' lần',
      ),
    [details.materials],
  )

  const alerts = useMemo(() => {
    const values: { tone: string; title: string; text: string }[] = []
    if (!reports.some((row) => row.reportDate === today)) {
      values.push({
        tone: 'orange',
        title: 'Chưa có báo cáo hôm nay',
        text: 'Hãy tạo báo cáo ca hoặc nhập dữ liệu Excel cho ngày hiện tại.',
      })
    }
    if (workflow.change > 0) {
      values.push({
        tone: 'red',
        title: `${workflow.change} báo cáo bị yêu cầu sửa`,
        text: 'Ưu tiên mở lại báo cáo CHANGE_REQUESTED và bổ sung nội dung quản lý yêu cầu.',
      })
    }
    if (metrics.attainment > 0 && metrics.attainment < 90) {
      values.push({
        tone: 'orange',
        title: 'Sản lượng dưới 90% kế hoạch',
        text: `Còn thiếu ${nf.format(Math.abs(metrics.gap))} sản phẩm; mức hoàn thành ${percentage(metrics.attainment)}.`,
      })
    }
    if (metrics.defectRate > 3)
      values.push({
        tone: 'red',
        title: 'Tỷ lệ lỗi cao',
        text: `Hàng lỗi chiếm ${percentage(metrics.defectRate)} sản lượng thực tế.`,
      })
    if (metrics.downtimeRate > 10) {
      values.push({
        tone: 'orange',
        title: 'Downtime cần chú ý',
        text: `Thời gian dừng chiếm ${percentage(metrics.downtimeRate)} thời gian làm việc.`,
      })
    }
    if (values.length === 0)
      values.push({
        tone: 'green',
        title: 'Tình hình trong ngưỡng',
        text: 'Chưa phát hiện cảnh báo lớn trong khoảng thời gian đang xem.',
      })
    return values
  }, [metrics, reports, today, workflow.change])

  const detailReports = useMemo(
    () => [...filtered].sort((a, b) => b.reportDate.localeCompare(a.reportDate) || b.id - a.id),
    [filtered],
  )
  const detailValue = (value: number) => {
    if (detailsLoading) return '…'
    if (detailsError) return '—'
    return nf.format(value)
  }
  const downtimeAverage = detailsLoading
    ? '…'
    : detailsError || details.downtime.length === 0
      ? '—'
      : `${nf.format(metrics.downtime / details.downtime.length)} phút`
  const peopleCountLabel = (reportId: number) => {
    if (detailsLoading) return '…'
    if (detailsError) return '—'

    const peopleCount = peopleByReport.get(reportId)
    return peopleCount === undefined ? '—' : `${peopleCount} người`
  }

  return (
    <section className="team-dashboard">
      <div className="team-dashboard-heading">
        <div>
          <h3>Dashboard vận hành của tổ</h3>
          <p>
            Từ {fromDate ? dateLabel(fromDate) : 'đầu kỳ'} đến{' '}
            {toDate ? dateLabel(toDate) : 'hiện tại'} · {filtered.length} báo cáo
          </p>
        </div>
        <div className="period-switch">
          {[
            { value: 1, label: 'Hôm nay' },
            { value: 7, label: '7 ngày' },
            { value: 30, label: '30 ngày' },
          ].map((item) => (
            <button
              key={item.value}
              className={days === item.value ? 'active' : ''}
              onClick={() => selectPeriod(item.value)}
            >
              {item.label}
            </button>
          ))}
        </div>
      </div>

      <div className="filters hr-filters">
        <label>
          Từ ngày
          <input
            type="date"
            value={fromDate}
            onChange={(event) => {
              setDays(0)
              setFromDate(event.target.value)
            }}
          />
        </label>
        <label>
          Đến ngày
          <input
            type="date"
            value={toDate}
            onChange={(event) => {
              setDays(0)
              setToDate(event.target.value)
            }}
          />
        </label>
        <label>
          Tìm báo cáo
          <input
            type="search"
            placeholder="Máy, ca, trạng thái, ghi chú…"
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
          />
        </label>
        <button type="button" onClick={clearFilters}>
          Xóa bộ lọc
        </button>
      </div>
      {detailsError && <p className="form-message error">{detailsError}</p>}

      <div className="team-insight-grid">
        <InsightCard
          tone="blue"
          label="Tiến độ sản xuất"
          main={percentage(metrics.attainment)}
          interpretation={
            metrics.attainment >= 100
              ? 'Đã đạt hoặc vượt kế hoạch.'
              : `Còn thiếu ${nf.format(Math.max(-metrics.gap, 0))} sản phẩm.`
          }
          rows={[
            ['Kế hoạch', nf.format(metrics.planned)],
            ['Thực tế', nf.format(metrics.actual)],
            ['Chênh lệch', signed(metrics.gap)],
          ]}
        />
        <InsightCard
          tone="green"
          label="Chất lượng đầu ra"
          main={`${nf.format(metrics.good)} tốt`}
          interpretation={
            metrics.defectRate <= 3
              ? 'Tỷ lệ lỗi đang trong ngưỡng theo dõi.'
              : 'Tỷ lệ lỗi cao, cần xem nhóm lỗi bên dưới.'
          }
          rows={[
            ['Sản phẩm lỗi', nf.format(metrics.defect)],
            ['Tỷ lệ lỗi', percentage(metrics.defectRate)],
            ['Loại lỗi ghi nhận', detailValue(details.quality.length)],
          ]}
        />
        <InsightCard
          tone="orange"
          label="Dừng máy"
          main={`${nf.format(metrics.downtime)} phút`}
          interpretation={
            downtimeReasons[0]
              ? `Nguyên nhân lớn nhất: ${downtimeReasons[0].label}.`
              : 'Chưa có lần dừng máy chi tiết.'
          }
          rows={[
            ['Tỷ lệ thời gian dừng', percentage(metrics.downtimeRate)],
            ['Số lần dừng', detailValue(details.downtime.length)],
            ['Bình quân/lần', downtimeAverage],
          ]}
        />
        <InsightCard
          tone="purple"
          label="Nhân sự thực tế"
          main={
            detailsLoading ? 'Đang tải…' : detailsError ? '—' : `${nf.format(people.turns)} lượt`
          }
          interpretation={
            detailsError
              ? 'Không tải được dữ liệu nhân sự chi tiết.'
              : `${people.unique} nhân viên khác nhau đã tham gia trong kỳ.`
          }
          rows={[
            ['Vắng/nghỉ', detailValue(people.absent)],
            ['Đi muộn', detailValue(people.late)],
            ['Tăng ca', detailsError ? '—' : `${nf.format(people.overtime)} phút`],
          ]}
        />
      </div>

      <div className="team-dashboard-grid">
        <Panel title="Tiến độ và chất lượng theo ngày">
          <div className="team-trend">
            {trend.map(([date, value]) => {
              const rate = value.planned > 0 ? (value.actual * 100) / value.planned : 0
              return (
                <div className="team-trend-row rich" key={date}>
                  <span>
                    {date.slice(8, 10)}/{date.slice(5, 7)}
                  </span>
                  <div className="team-trend-track">
                    <i style={{ width: `${Math.min(rate, 100)}%` }} />
                  </div>
                  <b>
                    {nf.format(value.actual)} / {nf.format(value.planned)}
                  </b>
                  <small>{percentage(rate)}</small>
                  <em>
                    {nf.format(value.good)} tốt · {nf.format(value.defect)} lỗi ·{' '}
                    {nf.format(value.downtime)} phút dừng
                  </em>
                </div>
              )
            })}
            {trend.length === 0 && (
              <p className="dashboard-empty">Chưa có báo cáo trong khoảng thời gian này.</p>
            )}
          </div>
        </Panel>
        <Panel title="Việc cần xử lý">
          <div className="team-alerts">
            {alerts.map((alert, index) => (
              <article className={alert.tone} key={`${alert.title}-${index}`}>
                <span />
                <div>
                  <b>{alert.title}</b>
                  <p>{alert.text}</p>
                </div>
              </article>
            ))}
          </div>
        </Panel>
      </div>

      <Panel title="Phân tích nguyên nhân trong kỳ">
        <div className="team-breakdowns">
          <Breakdown
            title="Lý do dừng máy"
            rows={downtimeReasons}
            empty={detailsError ? 'Không tải được chi tiết dừng máy' : 'Chưa ghi nhận dừng máy'}
          />
          <Breakdown
            title="Loại lỗi chất lượng"
            rows={qualityTypes}
            empty={detailsError ? 'Không tải được chi tiết lỗi' : 'Chưa ghi nhận lỗi chi tiết'}
          />
          <Breakdown
            title="Sự cố vật tư"
            rows={materialTypes}
            empty={detailsError ? 'Không tải được chi tiết vật tư' : 'Chưa ghi nhận sự cố vật tư'}
          />
        </div>
      </Panel>

      <Panel title="Chi tiết từng báo cáo trong kỳ">
        <div className="table-wrap team-report-table">
          <table>
            <thead>
              <tr>
                <th>Ngày</th>
                <th>Ca</th>
                <th>Máy</th>
                <th>Kế hoạch</th>
                <th>Thực tế</th>
                <th>Chênh lệch</th>
                <th>Hoàn thành</th>
                <th>Hàng tốt</th>
                <th>Hàng lỗi</th>
                <th>Tỷ lệ lỗi</th>
                <th>Downtime</th>
                <th>Nhân sự</th>
                <th>Trạng thái</th>
                <th>Ghi chú</th>
              </tr>
            </thead>
            <tbody>
              {detailReports.map((row) => {
                const attainment =
                  row.plannedQuantity > 0 ? (row.actualQuantity * 100) / row.plannedQuantity : 0
                const defectRate =
                  row.actualQuantity > 0 ? (row.defectQuantity * 100) / row.actualQuantity : 0
                return (
                  <tr key={row.id}>
                    <td>
                      <b>{dateLabel(row.reportDate)}</b>
                    </td>
                    <td>{row.shiftName}</td>
                    <td>{row.machineCode}</td>
                    <td>{nf.format(row.plannedQuantity)}</td>
                    <td>{nf.format(row.actualQuantity)}</td>
                    <td
                      className={
                        row.actualQuantity - row.plannedQuantity < 0 ? 'negative' : 'positive'
                      }
                    >
                      {signed(row.actualQuantity - row.plannedQuantity)}
                    </td>
                    <td>{percentage(attainment)}</td>
                    <td>{nf.format(row.goodQuantity)}</td>
                    <td>{nf.format(row.defectQuantity)}</td>
                    <td>{percentage(defectRate)}</td>
                    <td>{nf.format(row.downtimeMinutes)} phút</td>
                    <td>{peopleCountLabel(row.id)}</td>
                    <td>
                      <StatusBadge value={row.status} />
                    </td>
                    <td className="report-note-cell">{row.note || '—'}</td>
                  </tr>
                )
              })}
              {detailReports.length === 0 && (
                <tr>
                  <td colSpan={14} className="empty">
                    Chưa có báo cáo trong khoảng thời gian này
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </Panel>

      <Panel title="Trạng thái luồng báo cáo">
        <div className="team-workflow">
          <div>
            <b>{workflow.draft}</b>
            <span>Đang nhập</span>
            <small>Cần hoàn thiện</small>
          </div>
          <div>
            <b>{workflow.change}</b>
            <span>Cần sửa</span>
            <small>Theo góp ý quản lý</small>
          </div>
          <div>
            <b>{workflow.submitted}</b>
            <span>Chờ duyệt</span>
            <small>Đã gửi quản lý</small>
          </div>
          <div>
            <b>{workflow.completed}</b>
            <span>Đã duyệt/chốt</span>
            <small>Dữ liệu chính thức</small>
          </div>
        </div>
      </Panel>
    </section>
  )
}

function InsightCard({
  tone,
  label,
  main,
  rows,
  interpretation,
}: {
  tone: string
  label: string
  main: string
  rows: [string, string][]
  interpretation: string
}) {
  return (
    <article className={`team-insight ${tone}`}>
      <span className="team-insight-label">{label}</span>
      <strong>{main}</strong>
      <div>
        {rows.map(([name, value]) => (
          <p key={name}>
            <span>{name}</span>
            <b>{value}</b>
          </p>
        ))}
      </div>
      <small>{interpretation}</small>
    </article>
  )
}

function group(
  rows: TableRow[],
  label: (row: TableRow) => string,
  amount: (row: TableRow) => number,
  suffix: string,
) {
  const values = new Map<string, number>()
  rows.forEach((row) => {
    const key = label(row)
    values.set(key, (values.get(key) ?? 0) + amount(row))
  })
  return [...values.entries()]
    .sort((a, b) => b[1] - a[1])
    .map(([name, value]) => ({
      label: name,
      value: `${nf.format(value)}${suffix}`,
      raw: value,
    }))
}

function Breakdown({
  title,
  rows,
  empty,
}: {
  title: string
  rows: { label: string; value: string; raw: number }[]
  empty: string
}) {
  const max = rows[0]?.raw ?? 1
  return (
    <section>
      <h4>{title}</h4>
      {rows.slice(0, 5).map((row) => (
        <div className="breakdown-row" key={row.label}>
          <p>
            <span>{row.label}</span>
            <b>{row.value}</b>
          </p>
          <i>
            <span style={{ width: `${(row.raw * 100) / max}%` }} />
          </i>
        </div>
      ))}
      {rows.length === 0 && <p className="breakdown-empty">{empty}</p>}
    </section>
  )
}

function signed(value: number) {
  return `${value > 0 ? '+' : ''}${nf.format(value)}`
}

const issueLabels: Record<string, string> = {
  SHORTAGE: 'Thiếu vật tư',
  LATE_DELIVERY: 'Giao chậm',
  WRONG_SPECIFICATION: 'Sai quy cách',
  DAMAGED: 'Hư hỏng',
  QUALITY_FAILED: 'Không đạt chất lượng',
  OTHER: 'Khác',
}

const issueLabel = (value: string) => issueLabels[value] ?? value
