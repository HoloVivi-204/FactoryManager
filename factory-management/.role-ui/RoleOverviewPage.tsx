import { dashboardApi } from '../api/dashboardApi'
import { useAuth } from '../context/AuthContext'
import { KpiCard, LoadingState, Panel } from '../components/ui'
import { number, percent } from '../utils/format'
import { useApi } from '../utils/useApi'
import { primaryRole, roleLabels } from '../routes/roleNavigation'

const descriptions={
  DIRECTOR:'Kết quả vận hành toàn công ty và các cảnh báo cần Ban giám đốc quan tâm.',
  FACTORY_MANAGER:'Máy móc, dây chuyền, chất lượng, nhân sự và tiến độ trong nhà máy phụ trách.',
  DEPARTMENT_MANAGER:'Nhân sự, chấm công, KPI và tình trạng hoạt động của phòng ban phụ trách.',
  FINANCE:'Chi phí, doanh thu, lợi nhuận và biến động tài chính trong phạm vi được phân quyền.',
  PRODUCTION_MANAGER:'Kế hoạch - thực tế, sản lượng, downtime, hàng lỗi và nhân sự theo ca.',
  TEAM_LEADER:'Tình hình thực tế của tổ/ca và trạng thái hoàn thành báo cáo sản xuất.'
} as const

export default function RoleOverviewPage(){
  const {user}=useAuth();const role=primaryRole(user?.roles)
  const summary=useApi(dashboardApi.summary,[]);const data=summary.data
  if(summary.loading)return <LoadingState loading error={summary.error}/>
  if(role==='FINANCE')return <><Title role={role}/><div className="role-dashboard-grid"><Panel title="Phân hệ tài chính"><div className="module-empty"><b>Chưa có dữ liệu tài chính chính thức</b><p>Backend cần bổ sung doanh thu, chi phí nguyên vật liệu, nhân công, điện nước, bảo trì, giá vốn và công nợ trước khi tính lợi nhuận.</p></div></Panel><Panel title="Dữ liệu vận hành liên quan"><div className="role-facts"><span>Sản lượng thực tế<b>{number(data?.actualQuantity)}</b></span><span>Sản phẩm lỗi<b>{number(data?.defectQuantity)}</b></span><span>Downtime<b>{number(data?.downtimeMinutes)} phút</b></span></div></Panel></div></>
  if(role==='DEPARTMENT_MANAGER')return <><Title role={role}/><div className="kpi-grid"><KpiCard label="NHÂN SỰ THỰC TẾ" value="—" hint="Chờ API tổng hợp phòng ban" tone="blue"/><KpiCard label="CHẤM CÔNG" value="—" hint="Chờ dữ liệu chấm công" tone="green"/><KpiCard label="KPI BỘ PHẬN" value={percent(data?.averageOee)} hint="KPI vận hành hiện có" tone="purple"/><KpiCard label="CẢNH BÁO" value="0" hint="Thông báo cần xử lý" tone="orange"/></div><ScopePanel data={data}/></>
  return <><Title role={role}/><div className="kpi-grid"><KpiCard label="KẾ HOẠCH" value={number(data?.plannedQuantity)} hint={data?.scopeName??'Phạm vi được cấp'} tone="blue"/><KpiCard label="SẢN LƯỢNG THỰC TẾ" value={number(data?.actualQuantity)} hint={`${number(data?.goodQuantity)} sản phẩm tốt`} tone="green"/><KpiCard label="OEE TRUNG BÌNH" value={percent(data?.averageOee)} hint={`${data?.reportCount??0} báo cáo chính thức`} tone="purple"/><KpiCard label="DOWNTIME" value={`${number(data?.downtimeMinutes)} phút`} hint={`${number(data?.defectQuantity)} sản phẩm lỗi`} tone="orange"/></div><ScopePanel data={data}/></>
}

function Title({role}:{role:ReturnType<typeof primaryRole>}){return <div className="page-title"><div><h2>{roleLabels[role]}</h2><p>{descriptions[role as keyof typeof descriptions]??'Thông tin trong phạm vi được phân quyền.'}</p></div></div>}
function ScopePanel({data}:{data:any}){return <Panel title="Phạm vi dữ liệu hiện tại"><div className="role-facts"><span>Phạm vi<b>{data?.scopeName??'Chưa xác định'}</b></span><span>Loại phạm vi<b>{data?.scopeType??'—'}</b></span><span>Chất lượng<b>{percent(data?.averageQuality)}</b></span><span>Hiệu suất<b>{percent(data?.averagePerformance)}</b></span></div></Panel>}
