import { useRef, useState } from 'react'
import {
  stagingExcelApi,
  type StagingExcelImportResult,
} from '../api/stagingExcelApi'
import { Panel } from './ui'

export default function StagingExcelImporter({ onImported }: { onImported: () => void | Promise<void> }) {
  const inputRef = useRef<HTMLInputElement>(null)
  const [file, setFile] = useState<File>()
  const [result, setResult] = useState<StagingExcelImportResult>()
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState('')

  async function preview() {
    if (!file) {
      setMessage('Hãy chọn file Excel trước khi kiểm tra.')
      return
    }
    setBusy(true)
    setMessage('')
    try {
      const value = await stagingExcelApi.preview(file)
      setResult(value)
      setMessage(value.valid
        ? 'File hợp lệ. Bạn có thể bấm Nhập dữ liệu.'
        : `File còn ${value.errors.length} lỗi. Chưa có dữ liệu nào được lưu.`)
    } catch (error) {
      setResult(undefined)
      setMessage((error as Error).message)
    } finally {
      setBusy(false)
    }
  }

  async function importData() {
    if (!file || !result?.valid) return
    setBusy(true)
    setMessage('')
    try {
      const value = await stagingExcelApi.import(file)
      setResult(value)
      if (value.imported) {
        setMessage(`Đã nhập ${value.totalRows} dòng vào dữ liệu nháp.`)
        await onImported()
      } else {
        setMessage(`File còn ${value.errors.length} lỗi nên chưa được lưu.`)
      }
    } catch (error) {
      setMessage((error as Error).message)
    } finally {
      setBusy(false)
    }
  }

  function choose(value?: File) {
    setFile(value)
    setResult(undefined)
    setMessage('')
  }

  return (
    <Panel title="Nhập dữ liệu cũ từ Excel">
      <div className="excel-import-intro">
        <div>
          <b>Một file, 5 nhóm dữ liệu</b>
          <p>Sản lượng · Nhân sự · Dừng máy · Chất lượng · Vật tư. Bạn có thể chỉ điền sheet cần nhập.</p>
        </div>
        <button disabled={busy} onClick={() => void stagingExcelApi.template()}>
          Tải file Excel mẫu
        </button>
      </div>

      <div className="excel-dropzone" onClick={() => inputRef.current?.click()}>
        <input
          ref={inputRef}
          type="file"
          accept=".xlsx,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
          onChange={(event) => choose(event.target.files?.[0])}
        />
        <span className="excel-file-icon">XLSX</span>
        <div>
          <b>{file?.name ?? 'Chọn file Excel đã điền dữ liệu'}</b>
          <p>{file ? `${(file.size / 1024).toFixed(1)} KB` : 'Định dạng .xlsx, tối đa 10 MB'}</p>
        </div>
      </div>

      <div className="excel-import-actions">
        <button disabled={busy || !file} onClick={() => void preview()}>
          {busy ? 'Đang xử lý…' : '1. Kiểm tra file'}
        </button>
        <button className="primary" disabled={busy || !file || !result?.valid || result.imported}
          onClick={() => void importData()}>
          2. Nhập vào báo cáo DRAFT
        </button>
      </div>

      {result && (
        <>
          <div className={`excel-validation ${result.valid ? 'valid' : 'invalid'}`}>
            <b>{result.valid ? 'File hợp lệ' : 'File cần chỉnh sửa'}</b>
            <span>Tổng {result.totalRows} dòng</span>
          </div>
          <div className="excel-count-grid">
            <Count label="Sản lượng" value={result.productionRows} />
            <Count label="Nhân sự" value={result.employeeRows} />
            <Count label="Dừng máy" value={result.downtimeRows} />
            <Count label="Chất lượng" value={result.qualityRows} />
            <Count label="Vật tư" value={result.materialRows} />
          </div>
          {result.imported && (
            <p className="excel-imported-summary">
              Báo cáo: tạo {result.createdReports}, cập nhật {result.updatedReports}. Chi tiết: tạo {result.createdDetails}, cập nhật {result.updatedDetails}.
            </p>
          )}
          {result.errors.length > 0 && (
            <div className="table-wrap excel-errors">
              <table>
                <thead><tr><th>Sheet</th><th>Dòng</th><th>Cột</th><th>Lỗi cần sửa</th><th>Giá trị</th></tr></thead>
                <tbody>
                  {result.errors.map((error, index) => (
                    <tr key={`${error.sheet}-${error.row}-${error.column}-${index}`}>
                      <td><b>{error.sheet}</b></td>
                      <td>{error.row || '—'}</td>
                      <td>{error.column || '—'}</td>
                      <td>{error.message}</td>
                      <td>{error.value || '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}
      {message && <p className="form-message">{message}</p>}
      <p className="hint-inline">Hệ thống kiểm tra toàn bộ file trước khi lưu. Nếu có một dòng lỗi thì không dòng nào được nhập.</p>
    </Panel>
  )
}

function Count({ label, value }: { label: string; value: number }) {
  return <div><span>{label}</span><b>{value}</b></div>
}
