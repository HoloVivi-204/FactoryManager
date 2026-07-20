import type { PageKey, Role } from '../types'

export type NavItem = { key: PageKey; label: string; icon: string }
export const rolePriority: Role[] = ['ADMIN','DIRECTOR','FACTORY_MANAGER','DEPARTMENT_MANAGER','FINANCE','PRODUCTION_MANAGER','TEAM_LEADER','EMPLOYEE']
export const primaryRole = (roles: Role[] = []) => rolePriority.find(role => roles.includes(role)) ?? 'EMPLOYEE'

export const roleLabels: Record<Role,string> = {
  ADMIN:'Quản trị hệ thống', DIRECTOR:'Ban giám đốc', FACTORY_MANAGER:'Quản lý nhà máy',
  DEPARTMENT_MANAGER:'Trưởng bộ phận', FINANCE:'Kế toán / Tài chính',
  PRODUCTION_MANAGER:'Quản lý sản xuất', TEAM_LEADER:'Tổ trưởng / Ca trưởng', EMPLOYEE:'Nhân viên'
}

export const navigation: Record<Role,NavItem[]> = {
  ADMIN:[{key:'admin-factories',label:'Nhà máy',icon:'▦'},{key:'admin-departments',label:'Phòng ban',icon:'▤'},{key:'admin-lines',label:'Dây chuyền',icon:'⇢'},{key:'admin-teams',label:'Tổ sản xuất',icon:'♙'},{key:'admin-employees',label:'Nhân viên',icon:'♟'},{key:'admin-shifts',label:'Ca làm việc',icon:'◷'},{key:'admin-machine-types',label:'Loại máy',icon:'⚙'},{key:'admin-machines',label:'Máy móc',icon:'⚒'},{key:'admin-downtime-reasons',label:'Lý do dừng máy',icon:'◉'},{key:'admin-quality-types',label:'Loại lỗi chất lượng',icon:'◇'},{key:'admin-materials',label:'Vật tư',icon:'▱'},{key:'admin-users',label:'Tài khoản & quyền',icon:'⌘'}],
  DIRECTOR:[
    {key:'overview',label:'Tổng quan công ty',icon:'◈'},{key:'reports',label:'Báo cáo chính thức',icon:'▤'},
    {key:'approval',label:'Báo cáo chờ duyệt',icon:'✓'},{key:'machines',label:'Máy móc',icon:'⚙'},
    {key:'downtime',label:'Dừng máy',icon:'◷'},{key:'quality',label:'Chất lượng',icon:'◇'},
    {key:'people',label:'Nhân sự vận hành',icon:'♙'},{key:'materials',label:'Vật tư',icon:'▱'}],
  FACTORY_MANAGER:[
    {key:'overview',label:'Tổng quan nhà máy',icon:'◈'},{key:'approval',label:'Phê duyệt báo cáo',icon:'✓'},
    {key:'machines',label:'Máy & dây chuyền',icon:'⚙'},{key:'downtime',label:'Dừng máy',icon:'◷'},
    {key:'quality',label:'Chất lượng',icon:'◇'},{key:'people',label:'Nhân sự vận hành',icon:'♙'},
    {key:'materials',label:'Vật tư vận hành',icon:'▱'}],
  DEPARTMENT_MANAGER:[
    {key:'overview',label:'Tổng quan bộ phận',icon:'◈'},{key:'people',label:'Nhân sự bộ phận',icon:'♙'},
    {key:'attendance',label:'Chấm công',icon:'◷'},{key:'kpi',label:'KPI bộ phận',icon:'◇'},
    {key:'leave',label:'Nghỉ phép',icon:'○'},{key:'notifications',label:'Cảnh báo',icon:'●'}],
  FINANCE:[
    {key:'finance',label:'Tổng quan tài chính',icon:'◈'},{key:'warehouses',label:'Quản lý kho',icon:'▣'},
    {key:'inventory',label:'Nhập - xuất - tồn',icon:'⇄'},{key:'reports',label:'Báo cáo sản xuất',icon:'▤'},
    {key:'notifications',label:'Cảnh báo chi phí',icon:'●'}],
  PRODUCTION_MANAGER:[
    {key:'overview',label:'Tổng quan sản xuất',icon:'◈'},{key:'approval',label:'Duyệt báo cáo ca',icon:'✓'},
    {key:'reports',label:'Báo cáo ngày',icon:'▤'},{key:'machines',label:'Máy & dây chuyền',icon:'⚙'},
    {key:'downtime',label:'Downtime',icon:'◷'},{key:'quality',label:'Hàng lỗi',icon:'◇'},
    {key:'people',label:'Nhân sự theo ca',icon:'♙'},{key:'materials',label:'Sự cố vật tư',icon:'▱'}],
  TEAM_LEADER:[
    {key:'overview',label:'Tình hình tổ / ca',icon:'◈'},{key:'entry',label:'Nhập báo cáo ca',icon:'＋'},
    {key:'downtime',label:'Máy & downtime',icon:'◷'},{key:'quality',label:'Lỗi chất lượng',icon:'◇'},
    {key:'people',label:'Nhân sự thực tế',icon:'♙'},{key:'materials',label:'Sự cố vật tư',icon:'▱'},
    {key:'notifications',label:'Thông báo',icon:'●'}],
  EMPLOYEE:[
    {key:'overview',label:'Trang cá nhân',icon:'◈'},{key:'schedule',label:'Lịch làm việc',icon:'▣'},
    {key:'attendance',label:'Chấm công & tăng ca',icon:'◷'},{key:'kpi',label:'KPI cá nhân',icon:'◇'},
    {key:'leave',label:'Nghỉ phép',icon:'○'},{key:'notifications',label:'Thông báo',icon:'●'}]
}
