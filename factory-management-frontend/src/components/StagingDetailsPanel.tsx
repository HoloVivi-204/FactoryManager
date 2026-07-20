import type { StagingDetailBundle } from '../types'
import { DataTable, LoadingState, Panel } from './ui'

export default function StagingDetailsPanel({
  data,
  loading,
  error,
}: {
  data?: StagingDetailBundle
  loading: boolean
  error?: string
}) {
  if (loading || error) return <LoadingState loading={loading} error={error} />
  if (!data) return null

  return (
    <div className="staging-detail-sections">
      <Panel title={`Dừng máy (${data['machine-downtime-staging'].length})`}>
        <DataTable
          rows={data['machine-downtime-staging']}
          columns={[
            { key: 'machineCode', label: 'Máy' },
            { key: 'downtimeReasonName', label: 'Nguyên nhân' },
            { key: 'startTime', label: 'Bắt đầu' },
            { key: 'endTime', label: 'Kết thúc' },
            { key: 'durationMinutes', label: 'Số phút' },
            { key: 'description', label: 'Mô tả' },
          ]}
        />
      </Panel>
      <Panel title={`Lỗi chất lượng (${data['quality-report-staging'].length})`}>
        <DataTable
          rows={data['quality-report-staging']}
          columns={[
            { key: 'qualityErrorTypeName', label: 'Loại lỗi' },
            { key: 'quantity', label: 'Số lượng' },
            { key: 'description', label: 'Mô tả' },
          ]}
        />
      </Panel>
      <Panel title={`Sự cố vật tư (${data['material-issue-staging'].length})`}>
        <DataTable
          rows={data['material-issue-staging']}
          columns={[
            { key: 'materialName', label: 'Vật tư' },
            { key: 'issueType', label: 'Loại sự cố' },
            { key: 'quantity', label: 'Số lượng' },
            { key: 'unit', label: 'Đơn vị' },
            { key: 'description', label: 'Mô tả' },
          ]}
        />
      </Panel>
      <Panel title={`Nhân sự thực tế (${data['employee-actual-staging'].length})`}>
        <DataTable
          rows={data['employee-actual-staging']}
          columns={[
            { key: 'employeeName', label: 'Nhân viên' },
            { key: 'workingMinutes', label: 'Phút làm' },
            { key: 'overtimeMinutes', label: 'Tăng ca' },
            { key: 'attendanceStatus', label: 'Có mặt' },
            { key: 'assignmentType', label: 'Phân công' },
            { key: 'description', label: 'Ghi chú' },
          ]}
        />
      </Panel>
    </div>
  )
}
