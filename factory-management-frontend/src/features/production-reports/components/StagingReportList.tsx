import type { ReactNode } from 'react'
import type { StagingReport } from '../../../shared/types'
import { DataTable, StatusBadge } from '../../../shared/ui/ui'

export default function StagingReportList({
  rows,
  action,
}: {
  rows: StagingReport[]
  action?: (report: StagingReport) => ReactNode
}) {
  const columns = [
    { key: 'id', label: 'ID' },
    { key: 'reportDate', label: 'Ngày' },
    { key: 'shiftName', label: 'Ca' },
    { key: 'factoryName', label: 'Nhà máy' },
    { key: 'teamName', label: 'Tổ' },
    { key: 'machineCode', label: 'Máy' },
    { key: 'actualQuantity', label: 'Thực tế' },
    { key: 'defectQuantity', label: 'Lỗi' },
    {
      key: 'status',
      label: 'Trạng thái',
      render: (report: StagingReport) => <StatusBadge value={report.status} />,
    },
    ...(action
      ? [{ key: 'action', label: 'Thao tác', render: (report: StagingReport) => action(report) }]
      : []),
  ]

  return <DataTable rows={rows} columns={columns} />
}
