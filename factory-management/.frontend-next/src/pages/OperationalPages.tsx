import { dashboardApi } from '../api/dashboardApi'
import { DataTable, LoadingState, Panel, StatusBadge } from '../components/ui'
import { useApi } from '../utils/useApi'

type Kind = 'machines' | 'downtime' | 'quality' | 'people' | 'materials'

const configs: Record<
  Kind,
  {
    title: string
    sub: string
    load: () => Promise<any[]>
    cols: readonly (readonly [string, string])[]
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
  const api = useApi(config.load, [kind])
  const columns = config.cols.map(([key, label]) => ({
    key,
    label,
    render: (row: any) =>
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
      <Panel title="Dữ liệu theo phạm vi JWT">
        <LoadingState loading={api.loading} error={api.error} />
        <DataTable rows={api.data ?? []} columns={columns} />
      </Panel>
    </>
  )
}
