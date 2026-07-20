import {
  AlertTriangle,
  BarChart3,
  Bot,
  ChevronDown,
  Database,
  Lightbulb,
  Maximize2,
  Send,
  Sparkles,
  Target,
  Trash2,
  X,
} from 'lucide-react'
import { createPortal } from 'react-dom'
import {
  useEffect,
  useMemo,
  useRef,
  useState,
  type FormEvent,
  type KeyboardEvent,
} from 'react'
import {
  Bar as RechartsBar,
  BarChart as RechartsBarChart,
  Cell,
  CartesianGrid,
  Legend,
  Line as RechartsLine,
  LineChart as RechartsLineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import '../ai-chat.css'
import { aiChatApi } from '../api/aiChatApi'
import { roleLabels } from '../routes/roleNavigation'
import type { Role } from '../types'
import type {
  AiChatDataStatus,
  AiChatResponse,
  AiDashboard,
  AiDashboardWidget,
  AiRecommendation,
} from '../types/aiChat'
import { toast } from '../utils/toast'

type ChatMessage =
  | { id: string; role: 'user'; content: string }
  | { id: string; role: 'assistant'; content: string; response: AiChatResponse }

const suggestionsByRole: Record<Role, string[]> = {
  ADMIN: [
    'Hôm nay tổng sản lượng toàn hệ thống là bao nhiêu?',
    'So sánh chi phí tháng này với tháng trước.',
    'Tôi có thể hỏi chatbot những dữ liệu gì?',
  ],
  DIRECTOR: [
    'So sánh doanh thu, chi phí và lợi nhuận tháng này với tháng trước.',
    'Năng suất năm nay giảm ở đâu và vì sao?',
    'Máy nào có chi phí bảo trì cao nhất trong quý này?',
  ],
  FACTORY_MANAGER: [
    'Hôm nay sản lượng nhà máy của tôi đạt bao nhiêu phần trăm kế hoạch?',
    'So sánh năng suất tháng này với tháng trước.',
    'Máy nào có chi phí bảo trì cao nhất trong quý này?',
  ],
  DEPARTMENT_MANAGER: [
    'Hôm nay sản lượng trong phạm vi bộ phận của tôi là bao nhiêu?',
    'So sánh năng suất tháng này với tháng trước.',
    'Tôi có thể hỏi chatbot những dữ liệu gì?',
  ],
  FINANCE: [
    'So sánh chi phí tháng này với tháng trước.',
    'Máy nào có chi phí bảo trì cao nhất trong quý này?',
    'Sản lượng tháng này thay đổi thế nào so với tháng trước?',
  ],
  PRODUCTION_MANAGER: [
    'Hôm nay sản lượng đạt bao nhiêu phần trăm kế hoạch?',
    'Năng suất tháng này giảm ở dây chuyền nào?',
    'Máy nào có chi phí bảo trì cao nhất trong quý này?',
  ],
  TEAM_LEADER: [
    'Hôm nay máy nào trong tổ bị lỗi hoặc dừng ngoài kế hoạch?',
    'Hôm nay nhân viên nào vắng, nghỉ phép, đi muộn hoặc về sớm?',
    'Hôm nay tổ của tôi có lỗi chất lượng và sự cố vật tư nào?',
  ],
  EMPLOYEE: [
    'Tôi có thể hỏi chatbot những dữ liệu gì?',
  ],
}

const statusLabels: Record<string, string> = {
  OFFICIAL: 'Dữ liệu chính thức',
  TEMPORARY_UNCONFIRMED: 'Dữ liệu tạm / chưa xác nhận',
  OFFICIAL_WITH_TEMPORARY: 'Chính thức + dữ liệu tạm',
  NO_BUSINESS_DATA: 'Chưa có dữ liệu phù hợp',
}

function statusLabel(status: AiChatDataStatus) {
  return statusLabels[status] ?? status
}

function formatAsOf(value?: string | null) {
  if (!value) return ''
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString('vi-VN')
}

export default function AiChatWidget({ workspaceRole }: { workspaceRole: Role }) {
  const [open, setOpen] = useState(false)
  const [draft, setDraft] = useState('')
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [expandedDashboard, setExpandedDashboard] = useState<AiDashboard | null>(null)
  const launcherRef = useRef<HTMLButtonElement>(null)
  const inputRef = useRef<HTMLTextAreaElement>(null)
  const endRef = useRef<HTMLDivElement>(null)
  const dashboardCloseRef = useRef<HTMLButtonElement>(null)
  const suggestions = useMemo(() => suggestionsByRole[workspaceRole], [workspaceRole])

  useEffect(() => {
    setMessages([])
    setDraft('')
    setError(null)
    setLoading(false)
    setOpen(false)
    setExpandedDashboard(null)
  }, [workspaceRole])

  useEffect(() => {
    if (!open) return
    inputRef.current?.focus()
    const onKeyDown = (event: globalThis.KeyboardEvent) => {
      if (event.key !== 'Escape') return
      if (expandedDashboard) {
        setExpandedDashboard(null)
        window.setTimeout(() => inputRef.current?.focus(), 0)
        return
      }
      setOpen(false)
      window.setTimeout(() => launcherRef.current?.focus(), 0)
    }
    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [open, expandedDashboard])

  useEffect(() => {
    if (!expandedDashboard) return
    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    window.setTimeout(() => dashboardCloseRef.current?.focus(), 0)
    return () => {
      document.body.style.overflow = previousOverflow
    }
  }, [expandedDashboard])

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth', block: 'end' })
  }, [messages, loading])

  async function ask(prompt?: string) {
    const message = (prompt ?? draft).trim()
    if (!message || loading) return

    setMessages((current) => [
      ...current,
      { id: crypto.randomUUID(), role: 'user', content: message },
    ])
    setDraft('')
    setError(null)
    setLoading(true)

    try {
      const response = await aiChatApi.ask(message, workspaceRole)
      setMessages((current) => [
        ...current,
        {
          id: crypto.randomUUID(),
          role: 'assistant',
          content: response.answer,
          response,
        },
      ])
    } catch (reason) {
      const messageText = reason instanceof Error
        ? reason.message
        : 'Không thể nhận câu trả lời từ trợ lý AI.'
      setError(messageText)
      toast.error(messageText)
    } finally {
      setLoading(false)
    }
  }

  function submit(event: FormEvent) {
    event.preventDefault()
    void ask()
  }

  function handleComposerKeyDown(event: KeyboardEvent<HTMLTextAreaElement>) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault()
      void ask()
    }
  }

  function clearConversation() {
    setMessages([])
    setError(null)
    setDraft('')
    inputRef.current?.focus()
  }

  return (
    <>
      {!open && (
        <button
          ref={launcherRef}
          type="button"
          className="ai-chat-launcher"
          aria-label="Mở trợ lý dữ liệu AI"
          aria-expanded="false"
          aria-controls="ai-chat-drawer"
          onClick={() => setOpen(true)}
        >
          <Sparkles size={19} aria-hidden="true" />
          <span>Trợ lý AI</span>
        </button>
      )}

      {open && (
        <section
          id="ai-chat-drawer"
          className="ai-chat-drawer"
          role="dialog"
          aria-labelledby="ai-chat-title"
        >
          <header className="ai-chat-header">
            <div className="ai-chat-heading">
              <span className="ai-chat-logo"><Bot size={20} aria-hidden="true" /></span>
              <div>
                <h2 id="ai-chat-title">Trợ lý dữ liệu AI</h2>
                <small>{roleLabels[workspaceRole]}</small>
              </div>
            </div>
            <div className="ai-chat-header-actions">
              {messages.length > 0 && (
                <button type="button" onClick={clearConversation} aria-label="Xóa cuộc trò chuyện">
                  <Trash2 size={17} aria-hidden="true" />
                </button>
              )}
              <button
                type="button"
                onClick={() => {
                  setOpen(false)
                  window.setTimeout(() => launcherRef.current?.focus(), 0)
                }}
                aria-label="Đóng trợ lý AI"
              >
                <X size={19} aria-hidden="true" />
              </button>
            </div>
          </header>

          <div className="ai-chat-scope-note">
            <Database size={15} aria-hidden="true" />
            <span>Chỉ truy vấn dữ liệu trong phạm vi tài khoản và vai trò đang mở.</span>
          </div>

          <div className="ai-chat-messages" aria-live="polite" aria-busy={loading}>
            {messages.length === 0 && (
              <div className="ai-chat-welcome">
                <span><Sparkles size={21} aria-hidden="true" /></span>
                <h3>Bạn muốn xem số liệu nào?</h3>
                <p>
                  Mỗi câu hỏi được xử lý độc lập. Trợ lý chỉ sử dụng các API dữ liệu mà
                  Spring Boot cho phép đối với vai trò của bạn.
                </p>
                <div className="ai-chat-suggestions">
                  {suggestions.map((suggestion) => (
                    <button
                      key={suggestion}
                      type="button"
                      disabled={loading}
                      onClick={() => void ask(suggestion)}
                    >
                      {suggestion}
                    </button>
                  ))}
                </div>
              </div>
            )}

            {messages.map((message) => (
              <article key={message.id} className={`ai-chat-message ${message.role}`}>
                <div className="ai-chat-bubble">{message.content}</div>
                {message.role === 'assistant' && (
                  <div className="ai-chat-evidence">
                    <span className={`ai-chat-status status-${message.response.dataStatus.toLowerCase()}`}>
                      {statusLabel(message.response.dataStatus)}
                    </span>

                    {message.response.warnings.length > 0 && (
                      <div className="ai-chat-warning">
                        <AlertTriangle size={15} aria-hidden="true" />
                        <div>
                          {message.response.warnings.map((warning) => (
                            <p key={warning}>{warning}</p>
                          ))}
                        </div>
                      </div>
                    )}

                    {(message.response.recommendations ?? []).length > 0 && (
                      <AiRecommendationList recommendations={message.response.recommendations ?? []} />
                    )}

                    {(message.response.dashboards ?? []).map((dashboard, index) => (
                      <AiDashboardView
                        key={`${dashboard.title}-${index}`}
                        dashboard={dashboard}
                        onExpand={() => setExpandedDashboard(dashboard)}
                      />
                    ))}

                    {message.response.sources.length > 0 && (
                      <details className="ai-chat-sources">
                        <summary>
                          <span>Nguồn dữ liệu ({message.response.sources.length})</span>
                          <ChevronDown size={15} aria-hidden="true" />
                        </summary>
                        <ul>
                          {message.response.sources.map((source, index) => (
                            <li key={`${source.type}-${source.label}-${index}`}>
                              <b>{source.label}</b>
                              <span>
                                {source.recordCount.toLocaleString('vi-VN')} bản ghi
                                {source.asOf ? ` · Cập nhật ${formatAsOf(source.asOf)}` : ''}
                              </span>
                              <small>{statusLabel(source.dataStatus)}</small>
                            </li>
                          ))}
                        </ul>
                      </details>
                    )}
                  </div>
                )}
              </article>
            ))}

            {loading && (
              <div className="ai-chat-thinking">
                <span /><span /><span />
                <b>Đang phân tích dữ liệu…</b>
              </div>
            )}

            {error && (
              <div className="ai-chat-error" role="alert">
                <AlertTriangle size={16} aria-hidden="true" />
                <div>
                  <b>Chưa thể trả lời</b>
                  <span>{error}</span>
                </div>
                <button
                  type="button"
                  disabled={loading}
                  onClick={() => void ask(messages[messages.length - 1]?.content)}
                >
                  Thử lại
                </button>
              </div>
            )}
            <div ref={endRef} />
          </div>

          <form className="ai-chat-composer" onSubmit={submit}>
            <label htmlFor="ai-chat-input">Nhập câu hỏi cho trợ lý AI</label>
            <div>
              <textarea
                ref={inputRef}
                id="ai-chat-input"
                rows={2}
                maxLength={2000}
                value={draft}
                disabled={loading}
                placeholder="Ví dụ: Sản lượng hôm nay đạt bao nhiêu phần trăm kế hoạch?"
                onChange={(event) => setDraft(event.target.value)}
                onKeyDown={handleComposerKeyDown}
              />
              <button
                type="submit"
                disabled={loading || !draft.trim()}
                aria-label="Gửi câu hỏi"
              >
                <Send size={18} aria-hidden="true" />
              </button>
            </div>
            <small>Enter để gửi · Shift + Enter để xuống dòng · {draft.length}/2000</small>
          </form>
        </section>
      )}

      {expandedDashboard && createPortal(
        <div
          className="ai-chat-dashboard-modal-backdrop"
          role="presentation"
          onMouseDown={(event) => {
            if (event.target === event.currentTarget) setExpandedDashboard(null)
          }}
        >
          <section
            className="ai-chat-dashboard-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="ai-chat-dashboard-modal-title"
          >
            <header className="ai-chat-dashboard-modal-header">
              <div>
                <span><BarChart3 size={18} aria-hidden="true" /></span>
                <div>
                  <small>Dashboard dữ liệu từ các bảng nghiệp vụ</small>
                  <h2 id="ai-chat-dashboard-modal-title">{expandedDashboard.title}</h2>
                </div>
              </div>
              <button
                ref={dashboardCloseRef}
                type="button"
                aria-label="Đóng dashboard mở rộng"
                onClick={() => {
                  setExpandedDashboard(null)
                  window.setTimeout(() => inputRef.current?.focus(), 0)
                }}
              >
                <X size={20} aria-hidden="true" />
              </button>
            </header>
            <div className="ai-chat-dashboard-modal-content">
              <AiDashboardContent dashboard={expandedDashboard} />
            </div>
          </section>
        </div>,
        document.body,
      )}
    </>
  )
}

function AiRecommendationList({
  recommendations,
}: {
  recommendations: AiRecommendation[]
}) {
  const priorityLabels: Record<string, string> = {
    HIGH: 'Ưu tiên cao',
    MEDIUM: 'Ưu tiên vừa',
    LOW: 'Theo dõi',
  }
  return (
    <section className="ai-chat-recommendations" aria-label="Khuyến nghị hành động">
      <header>
        <span><Lightbulb size={16} aria-hidden="true" /></span>
        <div>
          <b>Khuyến nghị hành động</b>
          <small>Tính từ dữ liệu chính thức trong phạm vi được xem</small>
        </div>
      </header>
      <div>
        {recommendations.map((recommendation, index) => (
          <article key={`${recommendation.title}-${index}`} className={`priority-${recommendation.priority.toLowerCase()}`}>
            <div className="ai-chat-recommendation-title">
              <strong>{recommendation.title}</strong>
              <span>{priorityLabels[recommendation.priority] ?? recommendation.priority}</span>
            </div>
            <p>{recommendation.action}</p>
            <details>
              <summary>Bằng chứng từ dữ liệu</summary>
              <small>{recommendation.evidence}</small>
            </details>
            <div className="ai-chat-recommendation-impact">
              <Target size={14} aria-hidden="true" />
              <span>
                <b>{recommendation.estimated ? 'Tác động ước tính' : 'Mục tiêu'}</b>
                <small>{recommendation.expectedImpact}</small>
              </span>
              {recommendation.estimated && <i>Ước tính</i>}
            </div>
          </article>
        ))}
      </div>
    </section>
  )
}

function AiDashboardView({
  dashboard,
  onExpand,
}: {
  dashboard: AiDashboard
  onExpand: () => void
}) {
  const widgetTypes = new Set((dashboard.widgets ?? []).map((widget) => widget.viewType))
  const description = widgetTypes.has('PROGRESS')
    ? 'tiến độ kế hoạch, biểu đồ so sánh và bảng chi tiết'
    : widgetTypes.has('LINE')
      ? 'xu hướng theo kỳ và bảng dữ liệu'
      : widgetTypes.has('GROUPED_BAR') || widgetTypes.has('BAR')
        ? 'biểu đồ so sánh và bảng dữ liệu'
        : widgetTypes.has('DONUT')
          ? 'cơ cấu trên tổng và bảng dữ liệu'
          : 'biểu đồ và bảng dữ liệu phù hợp với câu hỏi'
  return (
    <button type="button" className="ai-chat-dashboard-open" onClick={onExpand}>
      <span>
        <BarChart3 size={17} aria-hidden="true" />
        <span>
          <b>Xem dashboard trực quan</b>
          <small>{dashboard.title} · {description}</small>
        </span>
      </span>
      <Maximize2 size={17} aria-hidden="true" />
    </button>
  )
}

function AiDashboardContent({
  dashboard,
}: {
  dashboard: AiDashboard
}) {
  const chartData = dashboard.bars
    .map((bar) => ({ ...bar, value: Number(bar.value) || 0 }))
    .filter((bar) => bar.value >= 0)
  const pieData = chartData.filter((bar) => bar.value > 0)
  const chartColors = ['#477bf4', '#7659dd', '#19a779', '#ec9b2d', '#df4c57', '#2ba9bf', '#9a60cf', '#6d7f99']
  const totalValue = pieData.reduce((total, item) => total + item.value, 0)
  const totalUnit = pieData.find((item) => item.unit)?.unit ?? ''
  const shareData = pieData
    .map((item, index) => ({
      ...item,
      color: chartColors[index % chartColors.length],
      percentage: totalValue > 0 ? item.value * 100 / totalValue : 0,
    }))
    .sort((left, right) => right.value - left.value)
  const ratios = dashboard.ratios ?? []
  const widgets = dashboard.widgets ?? []
  const isAttendanceDashboard = dashboard.title.toLocaleLowerCase('vi-VN').includes('nhân sự')
  return (
      <div className="ai-chat-dashboard-body is-expanded">
        <header>
          <div><b>{dashboard.title}</b><small>{dashboard.subtitle}</small></div>
        </header>

        {dashboard.kpis.length > 0 && (
          <section className="ai-chat-dashboard-kpis">
            {dashboard.kpis.map((kpi, index) => (
              <article key={`${kpi.label}-${index}`} className={kpi.tone ?? 'blue'}>
                <span>{kpi.label}</span>
                <strong>{formatDashboardValue(kpi.value, 'number')}</strong>
                <small>{kpi.unit ?? ''}</small>
              </article>
            ))}
          </section>
        )}

        {widgets.length > 0 && (
          <section className="ai-chat-dashboard-widget-stack">
            {widgets.map((widget, index) => (
              <AiDashboardWidgetView
                key={`${widget.viewType}-${widget.title}-${index}`}
                widget={widget}
                index={index}
              />
            ))}
          </section>
        )}

        {widgets.length === 0 && ratios.length === 0 && totalValue === 0 && dashboard.rows.length === 0 && (
          <section className="ai-chat-dashboard-no-denominator">
            <span><AlertTriangle size={21} aria-hidden="true" /></span>
            <div>
              <b>{isAttendanceDashboard ? 'Chưa có mẫu số nhân sự của ca' : 'Chưa có dữ liệu để dựng biểu đồ'}</b>
              <p>
                {isAttendanceDashboard
                  ? 'Chưa tìm thấy lịch làm việc phù hợp trong work_schedule. Dashboard vẫn giữ các bản ghi thực tế bên dưới nhưng không tạo tỷ lệ 1/1 gây hiểu nhầm.'
                  : 'Không có bản ghi phù hợp với ngày và phạm vi đang hỏi. Bạn có thể đổi điều kiện rồi hỏi lại trợ lý.'}
              </p>
            </div>
          </section>
        )}

        {widgets.length === 0 && ratios.length > 0 && (
          <section className="ai-chat-dashboard-ratios">
            <div className="ai-chat-dashboard-ratios-heading">
              <div>
                <b>Tình hình thực tế trên tổng nhân sự của ca</b>
                <small>{ratios[0]?.context}</small>
              </div>
              <span>Mẫu số lấy từ lịch làm việc</span>
            </div>
            <div className="ai-chat-dashboard-ratio-grid">
              {ratios.map((ratio) => {
                const numerator = Number(ratio.numerator) || 0
                const denominator = Number(ratio.denominator) || 0
                const percentage = denominator > 0 ? numerator * 100 / denominator : 0
                const color = ratioToneColor(ratio.tone)
                return (
                  <article key={ratio.key}>
                    <div
                      className="ai-chat-dashboard-ratio-ring"
                      style={{
                        background: `conic-gradient(${color} ${Math.min(percentage, 100) * 3.6}deg, #e7edf5 0deg)`,
                      }}
                    >
                      <div><strong>{percentage.toFixed(0)}%</strong></div>
                    </div>
                    <div>
                      <b>{ratio.label}</b>
                      <p>
                        <strong>{formatDashboardValue(numerator, 'number')}</strong>
                        <span>/ {formatDashboardValue(denominator, 'number')} {ratio.unit ?? ''}</span>
                      </p>
                      <small>
                        {ratio.numeratorLabel ?? 'thực tế'} trên {ratio.denominatorLabel ?? 'tổng'}
                      </small>
                    </div>
                  </article>
                )
              })}
            </div>
          </section>
        )}

        {widgets.length === 0 && totalValue > 0 && (
          <div className="ai-chat-dashboard-visuals">
            <section className="ai-chat-dashboard-chart-panel">
              <div className="ai-chat-dashboard-chart-heading">
                <div><span>01</span><h3>Cơ cấu trên tổng</h3></div>
                <small>Mỗi phần thể hiện tỷ trọng của một nhóm trong toàn bộ dữ liệu</small>
              </div>
              <div className="ai-chat-dashboard-pie-canvas">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={shareData}
                      dataKey="value"
                      nameKey="label"
                      innerRadius={90}
                      outerRadius={138}
                      paddingAngle={3}
                    >
                      {shareData.map((entry, index) => (
                        <Cell key={`${entry.label}-${index}`} fill={entry.color} />
                      ))}
                    </Pie>
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>
                <div className="ai-chat-dashboard-donut-total">
                  <span>Tổng</span>
                  <strong>{formatDashboardValue(totalValue, 'number')}</strong>
                  <small>{totalUnit}</small>
                </div>
              </div>
            </section>

            <section className="ai-chat-dashboard-chart-panel">
              <div className="ai-chat-dashboard-chart-heading">
                <div><span>02</span><h3>Tỷ lệ từng nhóm</h3></div>
                <small>Xếp hạng theo phần trăm đóng góp trên tổng</small>
              </div>
              <div className="ai-chat-dashboard-total-strip" aria-label="Thanh cơ cấu 100 phần trăm">
                {shareData.map((item) => (
                  <span
                    key={item.label}
                    title={`${item.label}: ${item.percentage.toFixed(1)}%`}
                    style={{ width: `${item.percentage}%`, backgroundColor: item.color }}
                  />
                ))}
              </div>
              <div className="ai-chat-dashboard-share-list">
                {shareData.map((item, index) => (
                  <article key={`${item.label}-${index}`}>
                    <div>
                      <span style={{ backgroundColor: item.color }} />
                      <b>{item.label}</b>
                      <strong>{item.percentage.toFixed(1)}%</strong>
                    </div>
                    <p>
                      {formatDashboardValue(item.value, 'number')} / {formatDashboardValue(totalValue, 'number')} {item.unit ?? totalUnit}
                    </p>
                    <i>
                      <span style={{ width: `${Math.max(item.percentage, 1)}%`, backgroundColor: item.color }} />
                    </i>
                  </article>
                ))}
              </div>
            </section>
          </div>
        )}

        {widgets.length === 0 && dashboard.rows.length > 0 && dashboard.columns.length > 0 && (
          <section className="ai-chat-dashboard-table">
            <h4>Chi tiết ({dashboard.rows.length})</h4>
            <div>
              <table>
                <thead><tr>{dashboard.columns.map((column) => <th key={column.key}>{column.label}</th>)}</tr></thead>
                <tbody>
                  {dashboard.rows.map((row, rowIndex) => (
                    <tr key={rowIndex}>
                      {dashboard.columns.map((column) => (
                        <td
                          key={column.key}
                          title={String(formatDashboardValue(row[column.key], column.format))}
                        >
                          {formatDashboardValue(row[column.key], column.format)}
                        </td>
                      ))}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        )}
      </div>
  )
}

function AiDashboardWidgetView({
  widget,
  index,
}: {
  widget: AiDashboardWidget
  index: number
}) {
  const viewType = widget.viewType.toUpperCase()
  const rows = widget.rows ?? []
  const series = widget.series ?? []
  const categoryKey = widget.categoryKey ?? ''

  if (viewType === 'PROGRESS' || viewType === 'RATIO' || viewType === 'PLAN_ACTUAL') {
    const actual = Number(widget.numerator) || 0
    const planned = Number(widget.denominator) || 0
    const percentage = planned > 0 ? actual * 100 / planned : 0
    const difference = actual - planned
    const color = ratioToneColor(widget.tone)
    const comparisonData = [{
      label: 'Sản lượng',
      planned,
      actual,
    }]
    return (
      <article className="ai-chat-dashboard-widget ai-chat-dashboard-plan-widget">
        <WidgetHeading index={index} title={widget.title} context={widget.context} />
        <div className="ai-chat-dashboard-plan-layout">
          <div className="ai-chat-dashboard-plan-summary">
            <div className="ai-chat-dashboard-plan-values">
              <span>
                <small>{widget.denominatorLabel ?? 'Kế hoạch'}</small>
                <strong>{formatDashboardValue(planned, 'number')}</strong>
                <i>{widget.unit ?? ''}</i>
              </span>
              <span>
                <small>{widget.numeratorLabel ?? 'Thực tế'}</small>
                <strong>{formatDashboardValue(actual, 'number')}</strong>
                <i>{widget.unit ?? ''}</i>
              </span>
            </div>
            <div className="ai-chat-dashboard-plan-rate">
              <div>
                <b>{formatDashboardValue(percentage, 'percent')}</b>
                <span>mức độ hoàn thành</span>
              </div>
              <strong className={difference >= 0 ? 'is-positive' : 'is-negative'}>
                {difference > 0
                  ? `Vượt ${formatDashboardValue(difference, 'number')} ${widget.unit ?? ''}`
                  : difference < 0
                    ? `Còn thiếu ${formatDashboardValue(Math.abs(difference), 'number')} ${widget.unit ?? ''}`
                    : 'Đúng kế hoạch'}
              </strong>
            </div>
            <div className="ai-chat-dashboard-plan-progress" aria-label={`Đạt ${percentage.toFixed(2)} phần trăm`}>
              <span style={{ width: `${Math.min(Math.max(percentage, 0), 100)}%`, backgroundColor: color }} />
            </div>
            {percentage > 100 && (
              <small className="ai-chat-dashboard-plan-excess">Đã vượt 100% kế hoạch</small>
            )}
          </div>
          <div className="ai-chat-dashboard-plan-chart">
            <ResponsiveContainer width="100%" height="100%">
              <RechartsBarChart data={comparisonData} margin={{ top: 8, right: 12, left: 2, bottom: 2 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e6ebf2" />
                <XAxis dataKey="label" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 10 }} width={56} />
                <Tooltip />
                <Legend />
                <RechartsBar dataKey="planned" name={widget.denominatorLabel ?? 'Kế hoạch'} fill="#9aa8ba" radius={[6, 6, 0, 0]} />
                <RechartsBar dataKey="actual" name={widget.numeratorLabel ?? 'Thực tế'} fill={color} radius={[6, 6, 0, 0]} />
              </RechartsBarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </article>
    )
  }

  if (viewType === 'TABLE') {
    return (
      <article className="ai-chat-dashboard-widget">
        <WidgetHeading index={index} title={widget.title} context={widget.context} />
        <DashboardTable columns={widget.columns ?? []} rows={rows} />
      </article>
    )
  }

  const normalizedRows = rows.map((row) => {
    const normalized: Record<string, unknown> = { ...row }
    for (const item of series) normalized[item.key] = Number(row[item.key]) || 0
    return normalized
  })

  if (viewType === 'LINE') {
    return (
      <article className="ai-chat-dashboard-widget">
        <WidgetHeading index={index} title={widget.title} context={widget.context} />
        <div className="ai-chat-dashboard-dynamic-chart">
          <ResponsiveContainer width="100%" height="100%">
            <RechartsLineChart data={normalizedRows} margin={{ top: 8, right: 16, left: 2, bottom: 8 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e6ebf2" />
              <XAxis dataKey={categoryKey} tick={{ fontSize: 10 }} />
              <YAxis tick={{ fontSize: 10 }} width={58} />
              <Tooltip />
              <Legend />
              {series.map((item, seriesIndex) => (
                <RechartsLine
                  key={item.key}
                  type="monotone"
                  dataKey={item.key}
                  name={item.label}
                  stroke={seriesColor(item.tone, seriesIndex)}
                  strokeWidth={3}
                  dot={{ r: 3 }}
                  activeDot={{ r: 5 }}
                />
              ))}
            </RechartsLineChart>
          </ResponsiveContainer>
        </div>
      </article>
    )
  }

  if (viewType === 'DONUT') {
    const valueSeries = series[0]
    const donutRows = valueSeries
      ? normalizedRows
        .map((row) => ({ label: String(row[categoryKey] ?? '—'), value: Number(row[valueSeries.key]) || 0 }))
        .filter((row) => row.value > 0)
      : []
    const total = donutRows.reduce((sum, row) => sum + row.value, 0)
    return (
      <article className="ai-chat-dashboard-widget">
        <WidgetHeading index={index} title={widget.title} context={widget.context} />
        <div className="ai-chat-dashboard-widget-donut">
          <div className="ai-chat-dashboard-pie-canvas">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie data={donutRows} dataKey="value" nameKey="label" innerRadius={82} outerRadius={126} paddingAngle={3}>
                  {donutRows.map((row, rowIndex) => (
                    <Cell key={`${row.label}-${rowIndex}`} fill={seriesColor(undefined, rowIndex)} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
            <div className="ai-chat-dashboard-donut-total">
              <span>Tổng</span>
              <strong>{formatDashboardValue(total, 'number')}</strong>
              <small>{valueSeries?.unit ?? widget.unit ?? ''}</small>
            </div>
          </div>
          <div className="ai-chat-dashboard-share-list">
            {donutRows.map((row, rowIndex) => (
              <article key={`${row.label}-${rowIndex}`}>
                <div>
                  <span style={{ backgroundColor: seriesColor(undefined, rowIndex) }} />
                  <b>{row.label}</b>
                  <strong>{total > 0 ? formatDashboardValue(row.value * 100 / total, 'percent') : '0%'}</strong>
                </div>
                <p>{formatDashboardValue(row.value, 'number')} {valueSeries?.unit ?? widget.unit ?? ''}</p>
              </article>
            ))}
          </div>
        </div>
      </article>
    )
  }

  if (viewType === 'GROUPED_BAR' || viewType === 'BAR') {
    return (
      <article className="ai-chat-dashboard-widget">
        <WidgetHeading index={index} title={widget.title} context={widget.context} />
        <div className="ai-chat-dashboard-dynamic-chart">
          <ResponsiveContainer width="100%" height="100%">
            <RechartsBarChart data={normalizedRows} margin={{ top: 8, right: 16, left: 2, bottom: 8 }}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e6ebf2" />
              <XAxis dataKey={categoryKey} tick={{ fontSize: 10 }} interval={0} />
              <YAxis tick={{ fontSize: 10 }} width={62} />
              <Tooltip />
              <Legend />
              {series.map((item, seriesIndex) => (
                <RechartsBar
                  key={item.key}
                  dataKey={item.key}
                  name={item.label}
                  fill={seriesColor(item.tone, seriesIndex)}
                  radius={[5, 5, 0, 0]}
                />
              ))}
            </RechartsBarChart>
          </ResponsiveContainer>
        </div>
      </article>
    )
  }

  return null
}

function WidgetHeading({
  index,
  title,
  context,
}: {
  index: number
  title: string
  context?: string | null
}) {
  return (
    <div className="ai-chat-dashboard-chart-heading">
      <div><span>{String(index + 1).padStart(2, '0')}</span><h3>{title}</h3></div>
      {context && <small>{context}</small>}
    </div>
  )
}

function DashboardTable({
  columns,
  rows,
}: {
  columns: NonNullable<AiDashboardWidget['columns']>
  rows: Array<Record<string, unknown>>
}) {
  if (columns.length === 0 || rows.length === 0) {
    return <p className="ai-chat-dashboard-widget-empty">Chưa có dòng dữ liệu phù hợp để hiển thị.</p>
  }
  return (
    <div className="ai-chat-dashboard-table">
      <div>
        <table>
          <thead><tr>{columns.map((column) => <th key={column.key}>{column.label}</th>)}</tr></thead>
          <tbody>
            {rows.map((row, rowIndex) => (
              <tr key={rowIndex}>
                {columns.map((column) => (
                  <td key={column.key} title={String(formatDashboardValue(row[column.key], column.format))}>
                    {formatDashboardValue(row[column.key], column.format)}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

function seriesColor(tone?: string | null, index = 0) {
  const colors = ['#477bf4', '#7659dd', '#19a779', '#ec9b2d', '#df4c57', '#2ba9bf', '#9a60cf', '#6d7f99']
  if (tone) return ratioToneColor(tone)
  return colors[index % colors.length]
}

function ratioToneColor(tone?: string | null) {
  const colors: Record<string, string> = {
    blue: '#477bf4',
    green: '#19a779',
    red: '#df4c57',
    purple: '#7659dd',
    orange: '#ec9b2d',
    teal: '#2ba9bf',
    gray: '#7b899b',
  }
  return colors[tone ?? 'blue'] ?? colors.blue
}

function formatDashboardValue(value: unknown, format?: string) {
  if (value === null || value === undefined || value === '') return '—'
  if (format === 'number') {
    const number = Number(value)
    return Number.isFinite(number) ? number.toLocaleString('vi-VN') : String(value)
  }
  if (format === 'percent') {
    const number = Number(value)
    return Number.isFinite(number)
      ? `${number.toLocaleString('vi-VN', { minimumFractionDigits: 0, maximumFractionDigits: 2 })}%`
      : String(value)
  }
  if (format === 'currency') {
    const number = Number(value)
    return Number.isFinite(number)
      ? `${number.toLocaleString('vi-VN', { maximumFractionDigits: 0 })} ₫`
      : String(value)
  }
  if (format === 'duration') {
    const number = Number(value)
    return Number.isFinite(number) ? `${number.toLocaleString('vi-VN')} phút` : String(value)
  }
  if (format === 'date') {
    const raw = String(value)
    const parts = raw.slice(0, 10).split('-')
    return parts.length === 3 ? `${parts[2]}/${parts[1]}/${parts[0]}` : raw
  }
  if (format === 'datetime') {
    const date = new Date(String(value))
    return Number.isNaN(date.getTime()) ? String(value) : date.toLocaleString('vi-VN')
  }
  if (format === 'status') return statusLabel(String(value))
  return String(value)
}
