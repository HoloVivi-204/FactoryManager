import type { ReactNode } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import AiChatWidget from '../components/AiChatWidget'
import { useAuth } from '../context/AuthContext'
import {
  availableRoles,
  navigation,
  roleLabels,
  workspacePath,
} from '../routes/roleNavigation'
import type { Role } from '../types'

export default function DashboardLayout({
  activeRole,
  children,
}: {
  activeRole: Role
  children: ReactNode
}) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const roles = availableRoles(user?.roles)

  return (
    <div className="shell">
      <aside>
        <div className="brand">
          <b>FM</b>
          <div>
            <strong>FactoryOps</strong>
            <small>Management System</small>
          </div>
        </div>

        <div className="role-context">
          <small>KHÔNG GIAN LÀM VIỆC</small>
          {roles.length > 1 ? (
            <select
              aria-label="Chọn không gian làm việc"
              value={activeRole}
              onChange={(event) => navigate(workspacePath(event.target.value as Role))}
            >
              {roles.map((role) => (
                <option key={role} value={role}>
                  {roleLabels[role]}
                </option>
              ))}
            </select>
          ) : (
            <b>{roleLabels[activeRole]}</b>
          )}
        </div>

        <nav>
          {navigation[activeRole].map((item) => (
            <NavLink key={item.key} to={workspacePath(activeRole, item.key)}>
              <i>{item.icon}</i>
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-user">
          <b>{user?.employeeName || user?.username}</b>
          <span>{roleLabels[activeRole]}</span>
          <button onClick={() => void logout()}>Đăng xuất</button>
        </div>
      </aside>
      <main>
        <header className="topbar">
          <div>
            <small>{roleLabels[activeRole].toUpperCase()}</small>
            <h1>Factory Management</h1>
          </div>
          <div className="date">{new Date().toLocaleDateString('vi-VN')}</div>
        </header>
        <div className="content">{children}</div>
      </main>
      <AiChatWidget workspaceRole={activeRole} />
    </div>
  )
}
