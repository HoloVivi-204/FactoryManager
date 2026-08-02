import { useState } from 'react'
import { dashboardApi } from '../api/dashboardApi'
import { DataTable, LoadingState, Panel } from '../components/ui'
import { number, percent } from '../utils/format'
import { useApi } from '../utils/useApi'
import type { ProductionReport, ProductionReportDetails } from '../types'

export default function ReportsPage() {
  const [from, setFrom] = useState('')
  const [to, setTo] = useState('')
  const [query, setQuery] = useState('')
  const [details, setDetails] = useState<ProductionReportDetails>()
  const [detailOpen, setDetailOpen] = useState(false)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState('')

  const params = new URLSearchParams()
  if (from) params.set('fromDate', from)
  if (to) params.set('toDate', to)
  const api = useApi(() => dashboardApi.reports(params.toString()), `${from}|${to}`)

  const rows = (api.data ?? []).filter(
    (item) =>
      !query ||
      `${item.reportNo} ${item.machineName} ${item.machineCode} ${item.teamName}`
        .toLowerCase()
        .includes(query.toLowerCase()),
  )

  async function openDetails(report: ProductionReport) {
    setDetailOpen(true)
    setDetails(undefined)
    setDetailError('')
    setDetailLoading(true)
    try {
      setDetails(await dashboardApi.productionReportDetails(report.id))
    } catch (error) {
      setDetailError((error as Error).message)
    } finally {
      setDetailLoading(false)
    }
  }

  function closeDetails() {
    setDetailOpen(false)
    setDetails(undefined)
    setDetailError('')
  }

  return (
    <>
      <div className="page-title">
        <div>
          <h2>Báo cáo sản xuất</h2>
          <p>Dữ liệu chính thức đã duyệt, được backend lọc theo phạm vi JWT.</p>
        </div>
      </div>
      <Panel title="Bộ lọc">
        <div className="filters">
          <label>Từ ngày<input type="date" value={from} onChange={(event) => setFrom(event.target.value)} /></label>
          <label>Đến ngày<input type="date" value={to} onChange={(event) => setTo(event.target.value)} /></label>
          <label className="grow">
            Tìm nhanh
            <input
              placeholder="Mã báo cáo, máy, tổ…"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
          </label>
        </div>
      </Panel>
      <Panel title={`${rows.length} báo cáo`}>
        <LoadingState loading={api.loading} error={api.error} />
        <DataTable
          rows={rows}
          columns={[
            { key: 'reportNo', label: 'Mã báo cáo' },
            { key: 'reportDate', label: 'Ngày' },
            { key: 'factoryName', label: 'Nhà máy' },
            { key: 'teamName', label: 'Tổ' },
            { key: 'machineCode', label: 'Máy' },
            { key: 'actualQuantity', label: 'Thực tế', render: (row) => number(row.actualQuantity) },
            { key: 'defectQuantity', label: 'Lỗi', render: (row) => number(row.defectQuantity) },
            { key: 'oee', label: 'OEE', render: (row) => <b>{percent(row.oee)}</b> },
            { key: 'action', label: 'Thao tác', render: (row) => <button onClick={() => void openDetails(row)}>Chi tiết</button> },
          ]}
        />
      </Panel>

      {detailOpen && (
        <div className="modal-backdrop" onMouseDown={(event) => event.target === event.currentTarget && closeDetails()}>
          <div className="modal approval-modal">
            <div className="admin-modal-header">
              <div>
                <h2>Chi tiết báo cáo {details?.report.reportNo ?? ''}</h2>
                <p>Dữ liệu chính thức sau phê duyệt</p>
              </div>
              <button className="modal-close" onClick={closeDetails}>×</button>
            </div>
            <div className="approval-body">
              <LoadingState loading={detailLoading} error={detailError} />
              {details && <OfficialDetails data={details} />}
            </div>
          </div>
        </div>
      )}
    </>
  )
}

function OfficialDetails({ data }: { data: ProductionReportDetails }) {
  return (
    <div className="staging-detail-sections">
      <div className="review-stats">
        <span>Kế hoạch <b>{data.report.plannedQuantity}</b></span>
        <span>Thực tế <b>{data.report.actualQuantity}</b></span>
        <span>Đạt <b>{data.report.goodQuantity}</b></span>
        <span>Lỗi <b>{data.report.defectQuantity}</b></span>
        <span>OEE <b>{percent(data.report.oee)}</b></span>
      </div>
      <Panel title={`Dừng máy (${data.downtimes.length})`}>
        <DataTable rows={data.downtimes} columns={[
          { key: 'machineCode', label: 'Máy' },
          { key: 'downtimeReasonName', label: 'Nguyên nhân' },
          { key: 'startTime', label: 'Bắt đầu' },
          { key: 'endTime', label: 'Kết thúc' },
          { key: 'durationMinutes', label: 'Số phút' },
        ]} />
      </Panel>
      <Panel title={`Lỗi chất lượng (${data.qualityErrors.length})`}>
        <DataTable rows={data.qualityErrors} columns={[
          { key: 'qualityErrorTypeName', label: 'Loại lỗi' },
          { key: 'quantity', label: 'Số lượng' },
          { key: 'description', label: 'Mô tả' },
        ]} />
      </Panel>
      <Panel title={`Sự cố vật tư (${data.materialIssues.length})`}>
        <DataTable rows={data.materialIssues} columns={[
          { key: 'materialName', label: 'Vật tư' },
          { key: 'issueType', label: 'Sự cố' },
          { key: 'quantity', label: 'Số lượng' },
          { key: 'unit', label: 'Đơn vị' },
        ]} />
      </Panel>
      <Panel title={`Nhân sự thực tế (${data.employees.length})`}>
        <DataTable rows={data.employees} columns={[
          { key: 'employeeName', label: 'Nhân viên' },
          { key: 'workingMinutes', label: 'Phút làm' },
          { key: 'overtimeMinutes', label: 'Tăng ca' },
          { key: 'attendanceStatus', label: 'Có mặt' },
          { key: 'assignmentType', label: 'Phân công' },
        ]} />
      </Panel>
    </div>
  )
}
