import { Navigate, Route, Routes, useParams } from 'react-router-dom'
import { useAuth } from '../features/auth/context/useAuth'
import DashboardLayout from './layouts/DashboardLayout'
import LoginPage from '../features/auth/pages/LoginPage'
import AppRoutes from './routes/AppRoutes'
import {
  defaultPage,
  navigation,
  primaryRole,
  roleFromSlug,
  workspacePath,
} from '../shared/navigation/roleNavigation'
import type { PageKey } from '../shared/types'

function HomeRedirect() {
  const { user } = useAuth()
  const role = primaryRole(user?.roles)
  return <Navigate replace to={workspacePath(role)} />
}

function WorkspaceRoute() {
  const { user } = useAuth()
  const { roleSlug, pageKey } = useParams()
  const role = roleFromSlug(roleSlug)

  if (!role || !user?.roles.includes(role)) {
    const fallbackRole = primaryRole(user?.roles)
    return <Navigate replace to={workspacePath(fallbackRole)} />
  }

  const page = pageKey as PageKey
  if (!navigation[role].some((item) => item.key === page)) {
    return <Navigate replace to={workspacePath(role, defaultPage(role))} />
  }

  return (
    <DashboardLayout activeRole={role}>
      <AppRoutes role={role} page={page} />
    </DashboardLayout>
  )
}

export default function App() {
  const { user, loading } = useAuth()
  if (loading) return <div className="app-loading">Đang xác thực phiên đăng nhập…</div>
  if (!user) return <LoginPage />

  return (
    <Routes>
      <Route path="/" element={<HomeRedirect />} />
      <Route path="/workspace/:roleSlug/:pageKey" element={<WorkspaceRoute />} />
      <Route path="*" element={<HomeRedirect />} />
    </Routes>
  )
}
