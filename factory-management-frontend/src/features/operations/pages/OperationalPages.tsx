import { useEffect, useMemo, useState } from 'react'
import { dashboardApi } from '../../dashboard/api/dashboardApi'
import { DataTable, LoadingState, Panel, StatusBadge } from '../../../shared/ui/ui'
import { number } from '../../../shared/utils/format'
import { useApi } from '../../../shared/utils/useApi'
import type { TableRow } from '../../../shared/types'

type Kind = 'machines' | 'downtime' | 'quality' | 'people' | 'materials'

const configs: Record<
  Kind,
  {
    title: string
    sub: string
    load: () => Promise<TableRow[]>
    cols: readonly (readonly [string, string])[]
    filterKey?: string
    filterLabel?: string
  }
> = {
  machines: {
    title: 'Máy trong phạm vi báo cáo',
    sub: 'Danh sách máy xuất hiện trong các báo cáo ca thuộc phạm vi được cấp.',
    load: dashboardApi.machinesInScope,
    cols: [
      ['code', 'Mã máy'],
      ['name', 'Tên máy'],
      ['teamName', 'Tổ'],
    ],
    filterKey: 'teamName',
    filterLabel: 'Tổ',
  },
  downtime: {
    title: 'Dừng máy',
    sub: 'Các lần dừng máy trong báo cáo thuộc phạm vi được cấp.',
    load: () => dashboardApi.stagingDetailsInScope('machine-downtime-staging'),
    cols: [
      ['reportDate', 'Ngày'],
      ['teamName', 'Tổ'],
      ['machineCode', 'Máy'],
      ['downtimeReasonName', 'Nguyên nhân'],
      ['startTime', 'Bắt đầu'],
      ['endTime', 'Kết thúc'],
      ['durationMinutes', 'Số phút'],
      ['reportStatus', 'Báo cáo'],
    ],
    filterKey: 'downtimeReasonName',
    filterLabel: 'Nguyên nhân',
  },
  quality: {
    title: 'Lỗi chất lượng',
    sub: 'Lỗi sản phẩm trong báo cáo thuộc phạm vi được cấp.',
    load: () => dashboardApi.stagingDetailsInScope('quality-report-staging'),
    cols: [
      ['reportDate', 'Ngày'],
      ['teamName', 'Tổ'],
      ['qualityErrorTypeName', 'Loại lỗi'],
      ['quantity', 'Số lượng'],
      ['description', 'Mô tả'],
      ['reportStatus', 'Báo cáo'],
    ],
    filterKey: 'qualityErrorTypeName',
    filterLabel: 'Loại lỗi',
  },
  people: {
    title: 'Nhân sự thực tế',
    sub: 'Nhân viên và giờ công trong báo cáo thuộc phạm vi được cấp.',
    load: () => dashboardApi.stagingDetailsInScope('employee-actual-staging'),
    cols: [
      ['reportDate', 'Ngày'],
      ['teamName', 'Tổ'],
      ['employeeName', 'Nhân viên'],
      ['workingMinutes', 'Phút làm'],
      ['overtimeMinutes', 'Tăng ca'],
      ['attendanceStatus', 'Có mặt'],
      ['assignmentType', 'Phân công'],
      ['reportStatus', 'Báo cáo'],
    ],
    filterKey: 'attendanceStatus',
    filterLabel: 'Có mặt',
  },
  materials: {
    title: 'Sự cố vật tư',
    sub: 'Thiếu hụt, hư hỏng và vấn đề vật tư trong báo cáo thuộc phạm vi được cấp.',
    load: () => dashboardApi.stagingDetailsInScope('material-issue-staging'),
    cols: [
      ['reportDate', 'Ngày'],
      ['teamName', 'Tổ'],
      ['materialName', 'Vật tư'],
      ['issueType', 'Loại sự cố'],
      ['quantity', 'Số lượng'],
      ['unit', 'Đơn vị'],
      ['reportStatus', 'Báo cáo'],
    ],
    filterKey: 'issueType',
    filterLabel: 'Loại sự cố',
  },
}

const badgeKeys = new Set([
  'status',
  'active',
  'attendanceStatus',
  'assignmentType',
  'issueType',
  'reportStatus',
])

export default function OperationalPage({ kind }: { kind: Kind }) {
  const config = configs[kind]
  const api = useApi(config.load, kind)
  const [filters, setFilters] = useState({
    fromDate: '',
    toDate: '',
    category: '',
    reportStatus: '',
    keyword: '',
  })

  useEffect(() => {
    setFilters({ fromDate: '', toDate: '', category: '', reportStatus: '', keyword: '' })
  }, [kind])

  const categoryOptions = useMemo(() => {
    if (!config.filterKey) return []
    return [
      ...new Set(
        (api.data ?? []).map((row) => String(row[config.filterKey!] ?? '')).filter(Boolean),
      ),
    ].sort((a, b) => a.localeCompare(b, 'vi'))
  }, [api.data, config.filterKey])

  const filteredRows = useMemo(() => {
    const term = filters.keyword.trim().toLocaleLowerCase('vi')
    return (api.data ?? []).filter((row) => {
      const rowDate = String(row.reportDate ?? row.startTime ?? '').slice(0, 10)
      if (filters.fromDate && rowDate && rowDate < filters.fromDate) return false
      if (filters.toDate && rowDate && rowDate > filters.toDate) return false
      if (filters.category && String(row[config.filterKey ?? '']) !== filters.category) return false
      if (filters.reportStatus && String(row.reportStatus ?? '') !== filters.reportStatus)
        return false
      if (!term) return true
      return Object.values(row).some((value) =>
        String(value ?? '')
          .toLocaleLowerCase('vi')
          .includes(term),
      )
    })
  }, [api.data, config.filterKey, filters])

  const columns = config.cols.map(([key, label]) => ({
    key,
    label,
    render: (row: TableRow) =>
      badgeKeys.has(key) ? <StatusBadge value={row[key]} /> : String(row[key] ?? '—'),
  }))

  return (
    <>
      <div className="page-title">
        <div>
          <h2>{config.title}</h2>
          <p>{config.sub}</p>
        </div>
      </div>
      <Panel title="Tìm kiếm dữ liệu">
        <div className="filters hr-filters">
          {kind !== 'machines' && (
            <label>
              Từ ngày
              <input
                type="date"
                value={filters.fromDate}
                onChange={(event) => setFilters({ ...filters, fromDate: event.target.value })}
              />
            </label>
          )}
          {kind !== 'machines' && (
            <label>
              Đến ngày
              <input
                type="date"
                value={filters.toDate}
                onChange={(event) => setFilters({ ...filters, toDate: event.target.value })}
              />
            </label>
          )}
          {config.filterKey && (
            <label>
              {config.filterLabel}
              <select
                value={filters.category}
                onChange={(event) => setFilters({ ...filters, category: event.target.value })}
              >
                <option value="">Tất cả</option>
                {categoryOptions.map((value) => (
                  <option key={value} value={value}>
                    {value}
                  </option>
                ))}
              </select>
            </label>
          )}
          {kind !== 'machines' && (
            <label>
              Trạng thái báo cáo
              <select
                value={filters.reportStatus}
                onChange={(event) => setFilters({ ...filters, reportStatus: event.target.value })}
              >
                <option value="">Tất cả</option>
                <option value="DRAFT">Đang nhập</option>
                <option value="SUBMITTED">Chờ duyệt</option>
                <option value="CHANGE_REQUESTED">Cần sửa</option>
                <option value="APPROVED">Đã duyệt</option>
                <option value="LOCKED">Đã chốt</option>
              </select>
            </label>
          )}
          <label>
            Từ khóa
            <input
              type="search"
              placeholder={kind === 'machines' ? 'Mã máy, tên máy, tổ…' : 'Nội dung cần tìm…'}
              value={filters.keyword}
              onChange={(event) => setFilters({ ...filters, keyword: event.target.value })}
            />
          </label>
          <button
            type="button"
            onClick={() =>
              setFilters({ fromDate: '', toDate: '', category: '', reportStatus: '', keyword: '' })
            }
          >
            Xóa bộ lọc
          </button>
        </div>
      </Panel>
      <Panel
        title={`Dữ liệu theo phạm vi JWT (${filteredRows.length}/${number(api.data?.length)})`}
      >
        <LoadingState loading={api.loading} error={api.error} />
        <DataTable rows={filteredRows} columns={columns} />
      </Panel>
    </>
  )
}
