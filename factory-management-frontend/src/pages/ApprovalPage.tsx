import { useState } from 'react'
import { dashboardApi } from '../api/dashboardApi'
import { shiftReportApi } from '../api/shiftReportApi'
import StagingDetailsPanel from '../components/StagingDetailsPanel'
import StagingReportList from '../components/StagingReportList'
import { LoadingState, Panel } from '../components/ui'
import { useApi } from '../utils/useApi'
import type { StagingDetailBundle, StagingReport } from '../types'

export default function ApprovalPage() {
  const api = useApi(dashboardApi.submitted, [])
  const [selected, setSelected] = useState<StagingReport>()
  const [details, setDetails] = useState<StagingDetailBundle>()
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState('')
  const [comment, setComment] = useState('')
  const [message, setMessage] = useState('')
  const [busy, setBusy] = useState(false)

  async function review(report: StagingReport) {
    setSelected(report)
    setDetails(undefined)
    setComment('')
    setDetailError('')
    setDetailLoading(true)
    try {
      setDetails(await shiftReportApi.bundle(report.id))
    } catch (error) {
      setDetailError((error as Error).message)
    } finally {
      setDetailLoading(false)
    }
  }

  function close() {
    if (busy) return
    setSelected(undefined)
    setDetails(undefined)
    setComment('')
    setDetailError('')
  }

  async function act(type: 'approve' | 'change') {
    if (!selected) return
    if (type === 'change' && !comment.trim()) {
      setMessage('Vui lòng nhập rõ nội dung cần chỉnh sửa.')
      return
    }
    setBusy(true)
    setMessage('')
    try {
      if (type === 'approve') await dashboardApi.approve(selected.id, comment)
      else await dashboardApi.requestChange(selected.id, comment)
      setMessage(type === 'approve' ? 'Đã phê duyệt báo cáo.' : 'Đã yêu cầu chỉnh sửa.')
      close()
      api.reload()
    } catch (error) {
      setMessage((error as Error).message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <>
      <div className="page-title">
        <div>
          <h2>Phê duyệt báo cáo</h2>
          <p>Chỉ hiển thị báo cáo SUBMITTED thuộc phạm vi dữ liệu của tài khoản.</p>
        </div>
      </div>
      <Panel title={`${api.data?.length ?? 0} báo cáo đang chờ`}>
        <LoadingState loading={api.loading} error={api.error} />
        <StagingReportList
          rows={api.data ?? []}
          action={(report) => <button onClick={() => void review(report)}>Xem đủ chi tiết</button>}
        />
      </Panel>

      {selected && (
        <div className="modal-backdrop" onMouseDown={(event) => event.target === event.currentTarget && close()}>
          <div className="modal approval-modal">
            <div className="admin-modal-header">
              <div>
                <h2>Duyệt báo cáo #{selected.id}</h2>
                <p>{selected.reportDate} · {selected.shiftName} · {selected.teamName}</p>
              </div>
              <button className="modal-close" disabled={busy} onClick={close}>×</button>
            </div>
            <div className="approval-body">
              <div className="review-stats">
                <span>Kế hoạch <b>{selected.plannedQuantity}</b></span>
                <span>Thực tế <b>{selected.actualQuantity}</b></span>
                <span>Đạt <b>{selected.goodQuantity}</b></span>
                <span>Lỗi <b>{selected.defectQuantity}</b></span>
                <span>Downtime <b>{selected.downtimeMinutes} phút</b></span>
              </div>
              <StagingDetailsPanel data={details} loading={detailLoading} error={detailError} />
              <label className="field">
                <span>Nhận xét / lý do yêu cầu sửa</span>
                <textarea value={comment} onChange={(event) => setComment(event.target.value)} />
              </label>
              {message && <p className="form-message error">{message}</p>}
            </div>
            <div className="form-actions">
              <button disabled={busy} onClick={close}>Đóng</button>
              <button className="danger" disabled={busy || !comment.trim()} onClick={() => void act('change')}>
                Yêu cầu sửa
              </button>
              <button className="primary" disabled={busy || detailLoading || !!detailError} onClick={() => void act('approve')}>
                Phê duyệt
              </button>
            </div>
          </div>
        </div>
      )}

      {message && !selected && <p className="form-message">{message}</p>}
    </>
  )
}
