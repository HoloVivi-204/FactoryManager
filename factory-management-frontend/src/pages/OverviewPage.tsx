import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { dashboardApi } from '../api/dashboardApi'
import { KpiCard, LoadingState, Panel } from '../components/ui'
import { number, percent } from '../utils/format'
import { useApi } from '../utils/useApi'

const hasQualityBreakdown = (
  goodQuantity?: number | null,
  defectQuantity?: number | null,
) => goodQuantity != null && defectQuantity != null

export default function OverviewPage() {
  const summary = useApi(dashboardApi.summary)
  const reportResponse = useApi(() => dashboardApi.reports())

  if (summary.loading) return <LoadingState loading error={summary.error} />

  if (summary.error) {
    return <LoadingState loading={false} error={summary.error} />
  }

  const data = summary.data
  const reports = (reportResponse.data ?? []).slice(-8)
  const goodQuantity = data?.goodQuantity
  const defectQuantity = data?.defectQuantity
  const qualityBreakdownAvailable = hasQualityBreakdown(goodQuantity, defectQuantity)
  const pie = qualityBreakdownAvailable
    ? [
        { name: 'Tốt', value: goodQuantity, color: '#18a875' },
        { name: 'Lỗi', value: defectQuantity, color: '#f05d5e' },
      ]
    : []

  return (
    <>
      <div className="page-title">
        <div>
          <h2>Tổng quan sản xuất</h2>
          <p>Dữ liệu vận hành được tổng hợp từ báo cáo đã duyệt</p>
        </div>
      </div>

      <div className="kpi-grid">
        <KpiCard
          label="SẢN LƯỢNG THỰC TẾ"
          value={number(data?.actualQuantity)}
          hint={`${number(data?.plannedQuantity)} kế hoạch`}
          tone="blue"
        />
        <KpiCard
          label="SẢN PHẨM TỐT"
          value={number(data?.goodQuantity)}
          hint={`${percent(data?.averageQuality)} chất lượng`}
          tone="green"
        />
        <KpiCard
          label="OEE TRUNG BÌNH"
          value={percent(data?.averageOee)}
          hint={`${number(data?.reportCount)} báo cáo`}
          tone="purple"
        />
        <KpiCard
          label="THỜI GIAN DỪNG"
          value={`${number(data?.downtimeMinutes)} phút`}
          hint={`${number(data?.defectQuantity)} sản phẩm lỗi`}
          tone="orange"
        />
      </div>

      <div className="chart-grid">
        <Panel title="Hiệu suất theo báo cáo">
          <ResponsiveContainer width="100%" height={290}>
            <AreaChart data={reports}>
              <defs>
                <linearGradient id="oee" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0" stopColor="#356af4" stopOpacity={0.4} />
                  <stop offset="1" stopColor="#356af4" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="reportNo" tick={{ fontSize: 11 }} />
              <YAxis />
              <Tooltip />
              <Area dataKey="oee" stroke="#356af4" fill="url(#oee)" />
            </AreaChart>
          </ResponsiveContainer>
        </Panel>

        <Panel title="Tỷ lệ chất lượng">
          {qualityBreakdownAvailable ? (
            <ResponsiveContainer width="100%" height={290}>
              <PieChart>
                <Pie data={pie} dataKey="value" innerRadius={68} outerRadius={98}>
                  {pie.map((slice) => (
                    <Cell key={slice.name} fill={slice.color} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <div className="empty">Chưa có dữ liệu chất lượng</div>
          )}
        </Panel>
      </div>

      <Panel title="Sản lượng gần đây">
        <ResponsiveContainer width="100%" height={260}>
          <BarChart data={reports}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="reportNo" />
            <YAxis />
            <Tooltip />
            <Bar dataKey="plannedQuantity" fill="#cbd5e1" name="Kế hoạch" />
            <Bar dataKey="actualQuantity" fill="#356af4" name="Thực tế" />
          </BarChart>
        </ResponsiveContainer>
      </Panel>
    </>
  )
}
