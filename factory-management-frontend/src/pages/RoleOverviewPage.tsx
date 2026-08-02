import { dashboardApi } from '../api/dashboardApi'
import { KpiCard, LoadingState, Panel } from '../components/ui'
import TeamLeaderDashboard from '../components/TeamLeaderDashboard'
import { number, percent } from '../utils/format'
import { useApi } from '../utils/useApi'
import { roleLabels } from '../routes/roleNavigation'
import type { DashboardSummary, Role } from '../types'

const descriptions: Partial<Record<Role, string>> = {
  DIRECTOR: 'Kết quả vận hành toàn công ty và các cảnh báo cần Ban giám đốc quan tâm.',
  FACTORY_MANAGER: 'Máy móc, dây chuyền, chất lượng, nhân sự và tiến độ trong nhà máy phụ trách.',
  DEPARTMENT_MANAGER: 'Nhân sự, chấm công, KPI và tình trạng hoạt động của phòng ban phụ trách.',
  FINANCE: 'Chi phí, doanh thu, lợi nhuận và biến động tài chính trong phạm vi được phân quyền.',
  PRODUCTION_MANAGER: 'Kế hoạch - thực tế, sản lượng, downtime, hàng lỗi và nhân sự theo ca.',
  TEAM_LEADER: 'Tình hình thực tế của tổ/ca và trạng thái hoàn thành báo cáo sản xuất.',
}

export default function RoleOverviewPage({ role }: { role: Role }) {
  if (role === 'TEAM_LEADER') return <TeamLeaderOverview />
  return <StandardRoleOverview role={role} />
}

function TeamLeaderOverview() {
  const reports = useApi(dashboardApi.stagingMyScope)
  if (reports.loading) return <LoadingState loading error={reports.error} />
  return (
    <>
      <Title role="TEAM_LEADER" />
      {reports.error && <p className="form-message error">{reports.error}</p>}
      <TeamLeaderDashboard reports={reports.data ?? []} />
    </>
  )
}

function StandardRoleOverview({ role }: { role: Role }) {
  const summary = useApi(dashboardApi.summary, role)
  const data = summary.data
  if (summary.loading) return <LoadingState loading error={summary.error} />

  if (role === 'DEPARTMENT_MANAGER') {
    return (
      <>
        <Title role={role} />
        <div className="kpi-grid">
          <KpiCard label="NHÂN SỰ THỰC TẾ" value="—" hint="Chờ API tổng hợp phòng ban" tone="blue" />
          <KpiCard label="CHẤM CÔNG" value="—" hint="Chờ dữ liệu chấm công" tone="green" />
          <KpiCard label="KPI BỘ PHẬN" value={percent(data?.averageOee)} hint="KPI vận hành hiện có" tone="purple" />
          <KpiCard label="CẢNH BÁO" value="0" hint="Thông báo cần xử lý" tone="orange" />
        </div>
        <ScopePanel data={data} />
      </>
    )
  }

  return (
    <>
      <Title role={role} />
      <div className="kpi-grid">
        <KpiCard label="KẾ HOẠCH" value={number(data?.plannedQuantity)} hint={data?.scopeName ?? 'Phạm vi được cấp'} tone="blue" />
        <KpiCard label="SẢN LƯỢNG THỰC TẾ" value={number(data?.actualQuantity)} hint={`${number(data?.goodQuantity)} sản phẩm tốt`} tone="green" />
        <KpiCard label="OEE TRUNG BÌNH" value={percent(data?.averageOee)} hint={`${data?.reportCount ?? 0} báo cáo chính thức`} tone="purple" />
        <KpiCard label="DOWNTIME" value={`${number(data?.downtimeMinutes)} phút`} hint={`${number(data?.defectQuantity)} sản phẩm lỗi`} tone="orange" />
      </div>
      <ScopePanel data={data} />
    </>
  )
}

function Title({ role }: { role: Role }) {
  return (
    <div className="page-title">
      <div>
        <h2>{roleLabels[role]}</h2>
        <p>{descriptions[role] ?? 'Thông tin trong phạm vi được phân quyền.'}</p>
      </div>
    </div>
  )
}

function ScopePanel({ data }: { data?: DashboardSummary }) {
  return (
    <Panel title="Phạm vi dữ liệu hiện tại">
      <div className="role-facts">
        <span>Phạm vi<b>{data?.scopeName ?? 'Chưa xác định'}</b></span>
        <span>Loại phạm vi<b>{data?.scopeType ?? '—'}</b></span>
        <span>Chất lượng<b>{percent(data?.averageQuality)}</b></span>
        <span>Hiệu suất<b>{percent(data?.averagePerformance)}</b></span>
      </div>
    </Panel>
  )
}
