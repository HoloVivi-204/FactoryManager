import type { ReactNode } from 'react'
import type { PageKey } from '../types'
import { useAuth } from '../context/AuthContext'
import { navigation, primaryRole, roleLabels } from '../routes/roleNavigation'

export default function DashboardLayout({page,onNavigate,children}:{page:PageKey;onNavigate:(page:PageKey)=>void;children:ReactNode}){
  const {user,logout}=useAuth()
  const role=primaryRole(user?.roles)
  const nav=navigation[role]
  return <div className="shell"><aside>
    <div className="brand"><b>FM</b><div><strong>FactoryOps</strong><small>Management System</small></div></div>
    <div className="role-context"><small>KHÔNG GIAN LÀM VIỆC</small><b>{roleLabels[role]}</b></div>
    <nav>{nav.map(item=><button className={page===item.key?'active':''} onClick={()=>onNavigate(item.key)} key={item.key}><i>{item.icon}</i>{item.label}</button>)}</nav>
    <div className="sidebar-user"><b>{user?.employeeName}</b><span>{roleLabels[role]}</span><button onClick={logout}>Đăng xuất</button></div>
  </aside><main><header className="topbar"><div><small>{roleLabels[role].toUpperCase()}</small><h1>Factory Management</h1></div><div className="date">{new Date().toLocaleDateString('vi-VN')}</div></header><div className="content">{children}</div></main></div>
}
