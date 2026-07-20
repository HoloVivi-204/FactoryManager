import { useEffect,useState } from 'react'
import { useAuth } from './context/AuthContext'
import DashboardLayout from './layouts/DashboardLayout'
import AppRoutes from './routes/AppRoutes'
import LoginPage from './pages/LoginPage'
import type { PageKey } from './types'
import { primaryRole } from './routes/roleNavigation'

export default function App(){
  const {user,loading}=useAuth();const[page,setPage]=useState<PageKey>('overview')
  useEffect(()=>{if(user){const role=primaryRole(user.roles);setPage(role==='ADMIN'?'admin-factories':role==='FINANCE'?'finance':'overview')}},[user?.userId])
  if(loading)return <div className="app-loading">Đang xác thực phiên đăng nhập…</div>
  if(!user)return <LoginPage/>
  return <DashboardLayout page={page} onNavigate={setPage}><AppRoutes page={page}/></DashboardLayout>
}
