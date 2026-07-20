import type { PageKey, Role } from '../types'
import EmployeePortalPage from '../pages/EmployeePortalPage'
import AdminMasterPage from '../pages/AdminMasterPage'
import ReportsPage from '../pages/ReportsPage'
import OperationalPage from '../pages/OperationalPages'
import TeamLeaderReportPage from '../pages/TeamLeaderReportPage'
import ApprovalPage from '../pages/ApprovalPage'
import RoleOverviewPage from '../pages/RoleOverviewPage'
import FinancePage from '../pages/FinancePage'
import HrManagementPage, { type HrView } from '../pages/HrManagementPage'
import MaintenancePage, { type MaintenanceView } from '../pages/MaintenancePage'
import ExecutiveDashboardPage from '../pages/ExecutiveDashboardPage'

export default function AppRoutes({ role, page }: { role: Role; page: PageKey }) {
  if (role === 'DIRECTOR') return <ExecutiveDashboardPage />

  if (page.startsWith('maintenance-')) {
    return <MaintenancePage role={role} view={page as MaintenanceView} />
  }

  if (role === 'ADMIN') {
    const sections: Record<string, string> = {
      'admin-factories': 'factories',
      'admin-departments': 'departments',
      'admin-lines': 'production-lines',
      'admin-teams': 'teams',
      'admin-employees': 'employees',
      'admin-shifts': 'shifts',
      'admin-machine-types': 'machine-types',
      'admin-machines': 'machines',
      'admin-downtime-reasons': 'downtime-reasons',
      'admin-quality-types': 'quality-error-types',
      'admin-materials': 'materials',
      'admin-users': 'users',
    }
    return <AdminMasterPage section={sections[page] ?? 'factories'} />
  }

  if (role === 'EMPLOYEE') return <EmployeePortalPage view={page} />

  if (
    role === 'DEPARTMENT_MANAGER' &&
    ['schedule', 'attendance', 'kpi', 'leave', 'overtime', 'assignments', 'notifications'].includes(page)
  ) {
    return <HrManagementPage view={page as HrView} />
  }

  if (role === 'FINANCE' && ['finance', 'warehouses', 'inventory'].includes(page)) {
    return <FinancePage view={page as 'finance' | 'warehouses' | 'inventory'} />
  }

  if (page === 'overview') return <RoleOverviewPage role={role} />
  if (page === 'entry') return <TeamLeaderReportPage />
  if (page === 'reports') return <ReportsPage />
  if (page === 'approval') return <ApprovalPage />

  if (['schedule', 'attendance', 'kpi', 'leave', 'overtime', 'assignments', 'notifications'].includes(page)) {
    return <RolePlaceholder page={page} />
  }

  return (
    <OperationalPage
      kind={page as 'machines' | 'downtime' | 'quality' | 'people' | 'materials'}
    />
  )
}

function RolePlaceholder({ page }: { page: PageKey }) {
  const names: Record<string, string> = {
    schedule: 'Lịch làm việc',
    attendance: 'Chấm công',
    kpi: 'KPI bộ phận',
    leave: 'Nghỉ phép',
    overtime: 'Đăng ký tăng ca',
    assignments: 'Điều chuyển nhân sự',
    notifications: 'Thông báo & cảnh báo',
  }
  return (
    <>
      <div className="page-title">
        <div>
          <h2>{names[page]}</h2>
          <p>Chức năng dành riêng cho phạm vi quản lý hiện tại.</p>
        </div>
      </div>
      <section className="panel">
        <div className="module-empty">
          <b>Module đang chờ API nghiệp vụ</b>
          <p>Dữ liệu sẽ xuất hiện khi backend có API tổng hợp tương ứng.</p>
        </div>
      </section>
    </>
  )
}
